package com.tommy.civictrack

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.tommy.civictrack.ai.MainViewModel
import com.tommy.civictrack.ai.ReportUiState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val repository = IssueRepository()
    private val viewModel: MainViewModel by viewModels()
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var issuesListener: ListenerRegistration? = null

    private var currentIssues: List<CivicIssue> = emptyList()
    private var selectedLocation: Location? = null
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String = ""
    private var selectedLocationMode = LocationMode.NONE
    private var activeTab = Screen.FEED
    private var activeFilter = "All Issues"
    private var detailsIssue: CivicIssue? = null

    private val currentUserEmail: String
        get() = firebaseAuth.currentUser?.email.orEmpty()

    private val isAdmin: Boolean
        get() = currentUserEmail.equals(BuildConfig.ADMIN_EMAIL, ignoreCase = true)

    private lateinit var contentFrame: FrameLayout
    private lateinit var topBrandIcon: ImageView
    private lateinit var topTitle: TextView
    private lateinit var topBellHost: FrameLayout
    private lateinit var topBell: ImageView
    private lateinit var topBellDot: View
    private lateinit var topAvatar: TextView

    private lateinit var feedTab: LinearLayout
    private lateinit var mapTab: LinearLayout
    private lateinit var reportTab: LinearLayout
    private lateinit var feedIcon: ImageView
    private lateinit var mapIcon: ImageView
    private lateinit var reportIcon: ImageView
    private lateinit var feedLabel: TextView
    private lateinit var mapLabel: TextView
    private lateinit var reportLabel: TextView

    private var reportTitleInput: EditText? = null
    private var reportDescriptionInput: EditText? = null
    private var reportCategorySpinner: Spinner? = null
    private var reportPrioritySpinner: Spinner? = null
    private var selectedPhotoText: TextView? = null
    private var selectedPhotoPreview: ImageView? = null
    private var currentLocationButton: MaterialButton? = null
    private var mapLocationButton: MaterialButton? = null
    private var reportLocationText: TextView? = null
    private var reportProgressBar: ProgressBar? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var osmMapView: MapView? = null
    private var mapBottomSheetBehavior: BottomSheetBehavior<MaterialCardView>? = null
    private var noIssuesText: TextView? = null
    private var mapIssueImage: ImageView? = null
    private var mapIssueTitle: TextView? = null
    private var mapIssueDescription: TextView? = null
    private var mapIssueLocation: TextView? = null
    private var mapIssueStatus: TextView? = null
    private var mapIssueStatusDot: View? = null
    private var mapIssueType: TextView? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            updateSelectedPhotoState()
            viewModel.processImage(contentResolver, it)
        }
    }

    private val mapPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult
            val latitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, Double.NaN)
            val longitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, Double.NaN)
            if (!latitude.isNaN() && !longitude.isNaN()) {
                setSelectedCoordinates(latitude, longitude, LocationMode.MAP)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (firebaseAuth.currentUser == null) {
            openLogin()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, bars.top, 0, 0)
            insets
        }

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        bindShell()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        wireShell()
        observeIssues()
        observeViewModel()
        showFeed()
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser == null) {
            openLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        osmMapView?.onResume()
    }

    override fun onPause() {
        osmMapView?.onPause()
        super.onPause()
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            reportProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.aiResponse.observe(this) { ai ->
            reportTitleInput?.setText(ai.title)
            reportDescriptionInput?.setText(ai.description)

            val categoryIndex = when (ai.category.lowercase()) {
                "roads", "pothole" -> 0
                "lighting", "streetlight" -> 1
                "sanitation", "garbage" -> 2
                "vandalism" -> 3
                else -> 0
            }
            reportCategorySpinner?.setSelection(categoryIndex)

            val priorityIndex = when (ai.priority) {
                "High" -> 0
                "Medium" -> 1
                "Low" -> 2
                else -> 1
            }
            reportPrioritySpinner?.setSelection(priorityIndex)

            toast("AI has analyzed the image!")
        }

        viewModel.imageUrl.observe(this) { url ->
            uploadedImageUrl = url
        }

        viewModel.uiState.observe(this) { state ->
            if (state is ReportUiState.Error) {
                toast("AI Error: ${state.message}")
            }
        }
    }

    override fun onDestroy() {
        issuesListener?.remove()
        super.onDestroy()
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                captureCurrentLocation {
                    if (activeTab == Screen.REPORT) {
                        updateLocationLabel()
                        updateLocationModeButtons()
                    }
                }
                if (activeTab == Screen.MAP) {
                    centerMapOnUser()
                }
            } else if (activeTab == Screen.MAP) {
                moveMapToDefaultCity()
                toast("Location permission denied. Showing Pune instead.")
            }
        }
    }

    private fun bindShell() {
        contentFrame = findViewById(R.id.contentFrame)
        topBrandIcon = findViewById(R.id.topBrandIcon)
        topTitle = findViewById(R.id.topTitle)
        topBellHost = findViewById(R.id.topBellHost)
        topBell = findViewById(R.id.topBell)
        topBellDot = findViewById(R.id.topBellDot)
        topAvatar = findViewById(R.id.topAvatar)

        feedTab = findViewById(R.id.feedTab)
        mapTab = findViewById(R.id.mapTab)
        reportTab = findViewById(R.id.reportTab)
        feedIcon = findViewById(R.id.feedIcon)
        mapIcon = findViewById(R.id.mapIcon)
        reportIcon = findViewById(R.id.reportIcon)
        feedLabel = findViewById(R.id.feedLabel)
        mapLabel = findViewById(R.id.mapLabel)
        reportLabel = findViewById(R.id.reportLabel)
    }

    private fun wireShell() {
        feedTab.setOnClickListener {
            hideMapIssueSheet()
            showFeed()
        }
        mapTab.setOnClickListener {
            hideMapIssueSheet()
            showMap()
        }
        reportTab.setOnClickListener {
            hideMapIssueSheet()
            showReport()
        }
        topBrandIcon.setOnClickListener {
            hideMapIssueSheet()
            if (activeTab == Screen.DETAILS) {
                showFeed()
            }
        }
        topBellHost.setOnClickListener {
            hideMapIssueSheet()
            toast("Notifications coming soon")
        }
        topAvatar.setOnClickListener {
            hideMapIssueSheet()
            signOutUser()
        }
    }

    private fun observeIssues() {
        issuesListener = repository.listenToIssues(
            onIssuesChanged = {
                currentIssues = it
                if (activeTab == Screen.MAP && osmMapView != null) {
                    renderIssueMarkers()
                } else {
                    refreshActiveScreen()
                }
            },
            onError = {
                toast("Using demo data while live issues are unavailable.")
            }
        )
    }

    private fun showFeed() {
        activeTab = Screen.FEED
        detailsIssue = null
        configureHeaderForHome()
        updateBottomNav()

        val root = screenColumn().apply {
            setPadding(16.dp(), 14.dp(), 16.dp(), 0)
        }
        root.addView(searchRow())
        root.addView(filterChips())

        filteredIssues().forEach { issue ->
            root.addView(feedCard(issue))
        }

        root.addView(bottomSpacer())
        setScreenContent(root, scrollable = true)
    }

    private fun showMap() {
        activeTab = Screen.MAP
        detailsIssue = null
        configureHeaderForHome()
        updateBottomNav()

        val view = layoutInflater.inflate(R.layout.view_map_screen, contentFrame, false)
        bindMapScreen(view)
        setScreenContent(view, scrollable = false)
        setupOsmMap()
        renderIssueMarkers()
        centerMapOnUser()
    }

    private fun bindMapScreen(root: View) {
        osmMapView = root.findViewById(R.id.osmMapView)
        noIssuesText = root.findViewById(R.id.noIssuesText)
        mapIssueImage = root.findViewById(R.id.mapIssueImage)
        mapIssueTitle = root.findViewById(R.id.mapIssueTitle)
        mapIssueDescription = root.findViewById(R.id.mapIssueDescription)
        mapIssueLocation = root.findViewById(R.id.mapIssueLocation)
        mapIssueStatus = root.findViewById(R.id.mapIssueStatus)
        mapIssueStatusDot = root.findViewById(R.id.mapIssueStatusDot)
        mapIssueType = root.findViewById(R.id.mapIssueType)

        val sheet = root.findViewById<MaterialCardView>(R.id.mapIssueSheet)
        mapBottomSheetBehavior = BottomSheetBehavior.from(sheet).apply {
            isHideable = true
            skipCollapsed = false
            peekHeight = 0
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        root.findViewById<ImageButton>(R.id.locationButton).setOnClickListener {
            hideMapIssueSheet()
            centerMapOnUser()
        }
        root.findViewById<ImageButton>(R.id.refreshButton).setOnClickListener {
            hideMapIssueSheet()
            renderIssueMarkers()
            toast("Map refreshed")
        }
        osmMapView?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN &&
                mapBottomSheetBehavior?.state != BottomSheetBehavior.STATE_HIDDEN
            ) {
                hideMapIssueSheet()
            }
            false
        }
    }

    private fun setupOsmMap() {
        val map = osmMapView ?: return
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(DEFAULT_CITY_ZOOM.toDouble())
        map.controller.setCenter(DEFAULT_CITY)
        map.overlays.clear()
        map.invalidate()
    }

    private fun renderIssueMarkers() {
        val map = osmMapView ?: return
        val issues = currentIssues.ifEmpty { resolveIssues() }
        map.overlays.removeAll { overlay -> overlay is Marker }
        hideMapIssueSheet()
        noIssuesText?.visibility = if (issues.isEmpty()) View.VISIBLE else View.GONE

        issues.forEach { issue ->
            val marker = Marker(map).apply {
                position = GeoPoint(issue.latitude, issue.longitude)
                title = issue.title
                snippet = issue.status
                icon = ContextCompat.getDrawable(this@MainActivity, markerDrawable(issue.category))
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                relatedObject = issue.id
                setOnMarkerClickListener { tappedMarker, _ ->
                    handleMarkerTap(tappedMarker)
                    true
                }
            }
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    private fun handleMarkerTap(marker: Marker) {
        val issueId = marker.relatedObject as? String ?: return
        val issue = resolveIssue(issueId) ?: return
        showMapIssueSheet(issue)
    }

    private fun showMapIssueSheet(issue: CivicIssue) {
        mapIssueTitle?.text = issue.title
        mapIssueDescription?.text = issue.description
        mapIssueLocation?.text = issueLocation(issue)
        mapIssueStatus?.text = issue.status
        mapIssueType?.text = issue.category

        val statusColor = if (issue.status.equals("Resolved", true)) {
            colorRes(R.color.civic_success)
        } else {
            colorRes(R.color.civic_error)
        }
        mapIssueStatusDot?.background = roundBg(statusColor, 5)
        mapIssueStatus?.background = roundBg(
            if (issue.status.equals("Resolved", true)) colorRes(R.color.civic_success_bg) else colorRes(R.color.civic_progress_bg),
            14
        )
        mapIssueStatus?.setTextColor(
            if (issue.status.equals("Resolved", true)) colorRes(R.color.civic_success) else colorRes(R.color.civic_primary)
        )

        val imageView = mapIssueImage ?: return
        if (issue.imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(issue.imageUrl)
                .placeholder(issueImageRes(issue))
                .into(imageView)
        } else {
            imageView.setImageResource(issueImageRes(issue))
        }

        mapBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMapIssueSheet() {
        mapBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun centerMapOnUser() {
        val map = osmMapView ?: return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            moveMapToDefaultCity()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    map.controller.animateTo(GeoPoint(location.latitude, location.longitude))
                    map.controller.setZoom(DEFAULT_CITY_ZOOM.toDouble())
                } else {
                    moveMapToDefaultCity()
                }
            }
            .addOnFailureListener {
                moveMapToDefaultCity()
            }
    }

    private fun moveMapToDefaultCity() {
        osmMapView?.controller?.apply {
            setZoom(DEFAULT_CITY_ZOOM.toDouble())
            setCenter(DEFAULT_CITY)
        }
    }

    private fun markerDrawable(category: String): Int {
        return when {
            category.contains("pothole", true) || category.contains("road", true) -> R.drawable.ic_marker_pothole
            category.contains("garbage", true) || category.contains("sanitation", true) || category.contains("waste", true) -> R.drawable.ic_marker_garbage
            category.contains("streetlight", true) || category.contains("lighting", true) -> R.drawable.ic_marker_streetlight
            else -> R.drawable.ic_marker_pothole
        }
    }

    private fun showReport() {
        activeTab = Screen.REPORT
        detailsIssue = null
        configureHeaderForHome()
        updateBottomNav()

        val root = screenColumn()
        root.addView(headlineBlock("Report New Issue", "Help improve your city by reporting local concerns."))
        root.addView(photoUploadCard())

        reportProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            visibility = if (viewModel.loading.value == true) View.VISIBLE else View.GONE
        }
        root.addView(reportProgressBar)

        root.addView(reportFormCard())
        root.addView(
            primaryButton(
                text = "Submit Report",
                onClick = { submitCurrentIssue() }
            )
        )
        root.addView(centerBody("By submitting, you agree to our Terms of Service and Privacy Policy."))
        root.addView(bottomSpacer())
        setScreenContent(root, scrollable = true)

        captureCurrentLocation { updateLocationLabel() }
    }

    private fun showMyIssues() {
        activeTab = Screen.MY_ISSUES
        detailsIssue = null
        configureHeaderForHome()
        updateBottomNav()

        val root = screenColumn()
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        headerRow.addView(
            headlineBlock("My Issues", "Track the reports you created and follow updates."),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        headerRow.addView(ghostButton("New Report") { showReport() })
        root.addView(headerRow)

        issuesForMyList().forEach { issue ->
            root.addView(myIssueCard(issue))
        }

        root.addView(bottomSpacer())
        setScreenContent(root, scrollable = true)
    }

    private fun showDetails(issue: CivicIssue) {
        activeTab = Screen.DETAILS
        detailsIssue = issue
        configureHeaderForDetails()
        updateBottomNav()

        val root = screenColumn()
        root.addView(detailsHeroCard(issue))
        root.addView(detailsLocationCard())
        root.addView(detailsTimelineCard())
        root.addView(detailsMetaCard(issue))
        root.addView(bottomSpacer())
        setScreenContent(root, scrollable = true)
    }

    private fun refreshActiveScreen() {
        when (activeTab) {
            Screen.FEED -> showFeed()
            Screen.MAP -> showMap()
            Screen.REPORT -> showReport()
            Screen.MY_ISSUES -> showFeed()
            Screen.DETAILS -> detailsIssue?.let { showDetails(resolveIssue(it.id) ?: it) }
        }
    }

    private fun configureHeaderForHome() {
        topBrandIcon.setImageResource(R.drawable.ic_civic_brand)
        topBrandIcon.background = null
        topBrandIcon.imageTintList = null
        topTitle.text = "CivicTrack"
        topBellHost.visibility = View.VISIBLE
        topBell.visibility = View.VISIBLE
        topBellDot.visibility = View.VISIBLE
        topAvatar.visibility = View.VISIBLE
        topAvatar.text = avatarInitials()
        topAvatar.background = roundStrokeBg(colorRes(R.color.civic_surface_low), colorRes(R.color.civic_outline), 20)
        topBrandIcon.setPadding(0, 0, 0, 0)
    }

    private fun configureHeaderForDetails() {
        topBrandIcon.setImageResource(R.drawable.ic_back)
        topBrandIcon.background = roundBg(colorRes(R.color.civic_surface_low), 18)
        topBrandIcon.setPadding(10.dp(), 10.dp(), 10.dp(), 10.dp())
        topTitle.text = "Issue Details"
        topBellHost.visibility = View.VISIBLE
        topBell.visibility = View.VISIBLE
        topBellDot.visibility = View.GONE
        topAvatar.visibility = View.GONE
    }

    private fun avatarInitials(): String {
        val email = currentUserEmail
        if (email.isBlank()) return "CT"
        val localPart = email.substringBefore("@")
        val pieces = localPart.split(".", "_", "-", " ").filter { it.isNotBlank() }
        return when {
            pieces.size >= 2 -> "${pieces.first().first()}${pieces[1].first()}".uppercase(Locale.getDefault())
            localPart.length >= 2 -> localPart.take(2).uppercase(Locale.getDefault())
            else -> localPart.uppercase(Locale.getDefault())
        }
    }

    private fun signOutUser() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            firebaseAuth.signOut()
            toast("Signed out")
            openLogin()
        }
    }

    private fun updateBottomNav() {
        val selected = if (activeTab == Screen.DETAILS) Screen.FEED else activeTab
        updateTab(feedTab, feedIcon, feedLabel, selected == Screen.FEED)
        updateTab(mapTab, mapIcon, mapLabel, selected == Screen.MAP)
        updateTab(reportTab, reportIcon, reportLabel, selected == Screen.REPORT)
    }

    private fun updateTab(container: LinearLayout, icon: ImageView, label: TextView, selected: Boolean) {
        container.background = null
        val tint = if (selected) colorRes(R.color.civic_primary) else colorRes(R.color.civic_muted)
        icon.setColorFilter(tint)
        label.setTextColor(tint)
        label.setTypeface(label.typeface, if (selected) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun filteredIssues(): List<CivicIssue> {
        val issues = resolveIssues()
        return when (activeFilter) {
            "Infrastructure" -> issues.filter { it.category.contains("road", true) || it.category.contains("pothole", true) || it.category.contains("light", true) }
            "Safety" -> issues.filter { it.priority.equals("High", true) || it.status.contains("review", true) || it.status.contains("progress", true) }
            "Sanitation" -> issues.filter { it.category.contains("sanitation", true) || it.category.contains("garbage", true) || it.category.contains("waste", true) }
            else -> issues
        }
    }

    private fun issuesForMyList(): List<CivicIssue> = currentIssues.take(4)

    private fun resolveIssues(): List<CivicIssue> {
        return currentIssues.ifEmpty {
            listOf(
                CivicIssue(
                    id = "CT-1042",
                    title = "Large pothole on Elm Street",
                    description = "A deep pothole has formed in the southbound lane and is creating a serious hazard for cyclists and smaller vehicles.",
                    category = "Roads",
                    latitude = 40.7128,
                    longitude = -74.0060,
                    status = "Under Review",
                    priority = "High"
                ),
                CivicIssue(
                    id = "CT-1043",
                    title = "Street light out near park",
                    description = "The lamp near the park entrance has been out for two nights, leaving the walkway dark after sunset.",
                    category = "Lighting",
                    latitude = 40.7142,
                    longitude = -74.0011,
                    status = "Assigned",
                    priority = "Medium"
                ),
                CivicIssue(
                    id = "CT-1044",
                    title = "Overflowing trash bins downtown",
                    description = "Garbage is spilling onto the sidewalk near the market block and needs sanitation pickup.",
                    category = "Sanitation",
                    latitude = 40.7111,
                    longitude = -74.0085,
                    status = "Pending",
                    priority = "High"
                )
            )
        }
    }

    private fun resolveIssue(id: String): CivicIssue? = resolveIssues().firstOrNull { it.id == id }

    private fun markIssueResolved(issue: CivicIssue) {
        repository.markIssueResolved(
            issueId = issue.id,
            onSuccess = {
                toast("Issue marked as resolved.")
            },
            onError = {
                toast("Resolve failed: ${it.message}")
            }
        )
    }

    private fun deleteIssue(issue: CivicIssue) {
        repository.deleteIssue(
            issueId = issue.id,
            onSuccess = {
                toast("Issue deleted.")
                if (activeTab == Screen.DETAILS) {
                    showFeed()
                }
            },
            onError = {
                toast("Delete failed: ${it.message}")
            }
        )
    }

    private fun adminIssueActions(issue: CivicIssue): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, 12.dp(), 0, 0)

            if (!issue.status.equals("Resolved", true)) {
                addView(compactAdminButton("Resolve", false) {
                    markIssueResolved(issue)
                }, LinearLayout.LayoutParams(wrapContent(), wrapContent()).apply {
                    rightMargin = 8.dp()
                })
            }

            addView(compactAdminButton("Delete", true) {
                deleteIssue(issue)
            })
        }
    }

    private fun feedCard(issue: CivicIssue): MaterialCardView {
        val card = elevatedCard()
        card.setOnClickListener { showDetails(issue) }

        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
        }

        val topMeta = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        topMeta.addView(priorityPill(issue.priority))
        topMeta.addView(metaText(issueAge(issue)).apply {
            gravity = Gravity.END
        }, LinearLayout.LayoutParams(0, wrapContent(), 1f))
        content.addView(topMeta)

        val mediaFrame = FrameLayout(this)
        mediaFrame.addView(issueImage(issue), FrameLayout.LayoutParams(matchParent(), 170.dp()))
        content.addView(mediaFrame, matchWrap().apply {
            topMargin = 14.dp()
            bottomMargin = 14.dp()
        })

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
        }
        titleRow.addView(TextView(this).apply {
            text = issue.title
            setTextColor(colorRes(R.color.civic_ink))
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, wrapContent(), 1f).apply {
            rightMargin = 12.dp()
        })
        titleRow.addView(statusPill(issue.status))
        content.addView(titleRow)
        content.addView(locationLine(issue))
        content.addView(bodyText(issue.description))
        content.addView(divider())

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        footer.addView(metaText(issueAge(issue)))
        footer.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        footer.addView(statusPill(issue.status))
        content.addView(footer)
        if (isAdmin) {
            content.addView(adminIssueActions(issue))
        }

        column.addView(content)
        card.addView(column)
        return card
    }

    private fun myIssueCard(issue: CivicIssue): MaterialCardView {
        val card = elevatedCard()
        card.setOnClickListener { showDetails(issue) }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(14.dp(), 14.dp(), 14.dp(), 14.dp())
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(issueImage(issue), LinearLayout.LayoutParams(92.dp(), 92.dp()).apply {
            rightMargin = 14.dp()
        })

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        topRow.addView(TextView(this).apply {
            text = issue.title
            setTextColor(colorRes(R.color.civic_ink))
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, wrapContent(), 1f).apply {
            rightMargin = 10.dp()
        })
        topRow.addView(statusPill(issue.status))
        content.addView(topRow)
        content.addView(locationLine(issue))
        content.addView(metaText("Report ID ${issue.id}"))
        content.addView(metaText(issueAge(issue)))
        if (isAdmin) {
            content.addView(adminIssueActions(issue).apply {
                setPadding(0, 10.dp(), 0, 0)
            })
        }

        row.addView(content, LinearLayout.LayoutParams(0, wrapContent(), 1f))
        card.addView(row)
        return card
    }

    private fun detailsHeroCard(issue: CivicIssue): MaterialCardView {
        val card = elevatedCard()
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(issueImage(issue), LinearLayout.LayoutParams(matchParent(), 250.dp()))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 18.dp(), 18.dp(), 18.dp())
        }

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
        }
        titleRow.addView(TextView(this).apply {
            text = issue.title
            setTextColor(colorRes(R.color.civic_ink))
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, wrapContent(), 1f).apply {
            rightMargin = 12.dp()
        })
        titleRow.addView(statusPill(issue.status))
        body.addView(titleRow)
        body.addView(locationLine(issue))
        body.addView(sectionLabel("Description"))
        body.addView(bodyText(issue.description))

        column.addView(body)
        card.addView(column)
        return card
    }

    private fun detailsLocationCard(): MaterialCardView {
        val card = elevatedCard()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 18.dp(), 18.dp(), 18.dp())
        }
        column.addView(sectionLabel("Location"))
        column.addView(ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.details_map)
            background = roundBg(colorRes(R.color.civic_surface_low), 18)
            clipToOutline = true
        }, LinearLayout.LayoutParams(matchParent(), 190.dp()))
        card.addView(column)
        return card
    }

    private fun detailsTimelineCard(): MaterialCardView {
        val card = elevatedCard()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 18.dp(), 18.dp(), 18.dp())
        }
        column.addView(sectionLabel("Issue Timeline"))
        column.addView(timelineItem("Reported", "Issue submitted by a resident with supporting photo evidence.", true))
        column.addView(timelineItem("Investigating", "Public Works team has scheduled an inspection and repair review.", true))
        column.addView(timelineItem("Fixed", "Estimated completion date will appear once the crew confirms the work order.", false))
        card.addView(column)
        return card
    }

    private fun detailsMetaCard(issue: CivicIssue): MaterialCardView {
        val card = elevatedCard()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 18.dp(), 18.dp(), 18.dp())
        }
        column.addView(sectionLabel("Details"))

        val grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        grid.addView(detailRow(
            detailChip("Priority", issue.priority, if (issue.priority.equals("High", true)) colorRes(R.color.civic_error) else colorRes(R.color.civic_primary)),
            detailChip("Category", issue.category, colorRes(R.color.civic_primary))
        ))
        grid.addView(detailRow(
            detailChip("Report ID", issue.id, colorRes(R.color.civic_ink)),
            View(this)
        ))
        column.addView(grid)
        card.addView(column)
        return card
    }

    private fun photoUploadCard(): MaterialCardView {
        val card = elevatedCard()
        card.strokeColor = colorRes(R.color.civic_outline)
        card.strokeWidth = 1.dp()

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 18.dp(), 18.dp(), 18.dp())
        }
        column.addView(sectionLabel("Photo"))

        val uploadPanel = FrameLayout(this).apply {
            background = roundDashedBg(colorRes(R.color.civic_card), colorRes(R.color.civic_primary), 20)
            setOnClickListener { imagePicker.launch("image/*") }
        }

        uploadPanel.addView(ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.report_upload_bg)
            alpha = 0.10f
        }, FrameLayout.LayoutParams(matchParent(), 220.dp()))

        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24.dp(), 24.dp(), 24.dp(), 24.dp())
        }

        center.addView(ImageView(this).apply {
            setImageResource(R.drawable.ic_camera)
            background = roundBg(colorRes(R.color.civic_primary_soft), 28)
            setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
        }, LinearLayout.LayoutParams(64.dp(), 64.dp()).apply {
            bottomMargin = 20.dp()
        })
        center.addView(TextView(this).apply {
            text = "Upload Photo or Video"
            setTextColor(colorRes(R.color.civic_ink))
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        center.addView(TextView(this).apply {
            text = "JPEG, PNG, MP4 up to 10MB"
            setTextColor(colorRes(R.color.civic_muted))
            textSize = 14f
            gravity = Gravity.CENTER
        }, matchWrap().apply {
            topMargin = 6.dp()
        })

        uploadPanel.addView(center, FrameLayout.LayoutParams(matchParent(), 220.dp()))
        column.addView(uploadPanel, matchWrap().apply {
            topMargin = 10.dp()
        })

        val previewRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 14.dp(), 0, 0)
        }
        selectedPhotoPreview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = roundBg(colorRes(R.color.civic_surface_container), 12)
            visibility = View.GONE
            clipToOutline = true
        }
        previewRow.addView(selectedPhotoPreview, LinearLayout.LayoutParams(60.dp(), 60.dp()).apply {
            rightMargin = 12.dp()
        })
        selectedPhotoText = TextView(this).apply {
            setTextColor(colorRes(R.color.civic_muted))
            textSize = 14f
            text = "No photo selected yet."
        }
        previewRow.addView(selectedPhotoText, LinearLayout.LayoutParams(0, wrapContent(), 1f))
        column.addView(previewRow)

        card.addView(column)
        updateSelectedPhotoState()
        return card
    }

    private fun reportFormCard(): MaterialCardView {
        val card = elevatedCard()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 18.dp(), 18.dp(), 18.dp())
        }
        column.addView(sectionLabel("Details"))

        reportCategorySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Roads", "Lighting", "Sanitation", "Vandalism")
            )
        }
        reportPrioritySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("High", "Medium", "Low")
            )
            setSelection(1)
        }
        reportTitleInput = formInput("Issue title", singleLine = true)
        reportDescriptionInput = formInput("Provide details about the issue...", singleLine = false)

        column.addView(fieldLabel("Category"))
        column.addView(reportCategorySpinner!!, matchWrap().apply {
            bottomMargin = 16.dp()
        })
        column.addView(fieldLabel("Title"))
        column.addView(reportTitleInput!!, matchWrap().apply {
            bottomMargin = 16.dp()
        })
        column.addView(fieldLabel("Description"))
        column.addView(reportDescriptionInput!!, matchWrap().apply {
            bottomMargin = 16.dp()
        })
        column.addView(fieldLabel("Priority"))
        column.addView(reportPrioritySpinner!!, matchWrap().apply {
            bottomMargin = 18.dp()
        })

        val locationHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        locationHeader.addView(fieldLabel("Location"), LinearLayout.LayoutParams(0, wrapContent(), 1f))
        column.addView(locationHeader)

        val locationModeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        currentLocationButton = locationModeButton("Use Current") {
            captureCurrentLocation {
                if (selectedLocation != null) {
                    selectedLocationMode = LocationMode.CURRENT
                }
                updateLocationLabel()
                updateLocationModeButtons()
            }
        }
        mapLocationButton = locationModeButton("Select on Map") {
            mapPickerLauncher.launch(
                Intent(this@MainActivity, MapPickerActivity::class.java).apply {
                    putExtra(MapPickerActivity.EXTRA_LATITUDE, selectedLocation?.latitude ?: DEFAULT_CITY.latitude)
                    putExtra(MapPickerActivity.EXTRA_LONGITUDE, selectedLocation?.longitude ?: DEFAULT_CITY.longitude)
                }
            )
        }
        locationModeRow.addView(currentLocationButton, LinearLayout.LayoutParams(0, wrapContent(), 1f).apply {
            rightMargin = 8.dp()
        })
        locationModeRow.addView(mapLocationButton, LinearLayout.LayoutParams(0, wrapContent(), 1f))
        column.addView(locationModeRow, matchWrap().apply {
            bottomMargin = 14.dp()
        })

        val mapFrame = FrameLayout(this)
        mapFrame.addView(ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.report_map)
            background = roundBg(colorRes(R.color.civic_surface_low), 18)
            clipToOutline = true
        }, FrameLayout.LayoutParams(matchParent(), 172.dp()))
        reportLocationText = TextView(this).apply {
            setTextColor(colorRes(R.color.white))
            textSize = 13f
            background = roundBg(colorRes(R.color.civic_card), 10)
            setPadding(12.dp(), 8.dp(), 12.dp(), 8.dp())
            setTextColor(colorRes(R.color.civic_primary))
        }
        mapFrame.addView(reportLocationText, FrameLayout.LayoutParams(wrapContent(), wrapContent()).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            leftMargin = 16.dp()
            bottomMargin = 16.dp()
        })
        column.addView(mapFrame)
        card.addView(column)
        updateLocationLabel()
        updateLocationModeButtons()
        return card
    }

    private fun submitCurrentIssue() {
        val title = reportTitleInput?.text?.toString().orEmpty().trim()
        val description = reportDescriptionInput?.text?.toString().orEmpty().trim()
        val category = reportCategorySpinner?.selectedItem?.toString().orEmpty()
        val priority = reportPrioritySpinner?.selectedItem?.toString().orEmpty()
        val location = selectedLocation

        when {
            title.isBlank() -> toast("Add a title first.")
            description.isBlank() -> toast("Add a description first.")
            location == null -> toast("Auto-detect the location before submitting.")
            currentUserEmail.isBlank() -> {
                toast("Please sign in again before submitting.")
                openLogin()
            }
            uploadedImageUrl.isBlank() && selectedImageUri != null -> {
                if (viewModel.loading.value == true) {
                    toast("Please wait for AI analysis to finish.")
                } else {
                    toast("Image processing failed. Try picking the image again.")
                }
            }
            else -> {
                val submit: (String) -> Unit = { imageUrl ->
                    repository.submitIssue(
                        title = title,
                        description = description,
                        category = category,
                        imageUrl = imageUrl,
                        priority = priority,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        userEmail = currentUserEmail,
                        onSuccess = {
                            toast("Issue submitted successfully.")
                            clearReportForm()
                            showFeed()
                        },
                        onError = {
                            toast("Submit failed: ${it.message}")
                        }
                    )
                }
                submit(uploadedImageUrl)
            }
        }
    }

    private fun clearReportForm() {
        selectedImageUri = null
        uploadedImageUrl = ""
        selectedLocation = null
        selectedLocationMode = LocationMode.NONE
        reportTitleInput?.setText("")
        reportDescriptionInput?.setText("")
        reportCategorySpinner?.setSelection(0)
        reportPrioritySpinner?.setSelection(1)
        updateSelectedPhotoState()
        updateLocationLabel()
        updateLocationModeButtons()
    }

    private fun updateSelectedPhotoState() {
        val preview = selectedPhotoPreview ?: return
        val text = selectedPhotoText ?: return
        if (selectedImageUri != null) {
            preview.visibility = View.VISIBLE
            preview.setImageURI(selectedImageUri)
            text.text = "Photo ready for upload"
            text.setTextColor(colorRes(R.color.civic_primary))
        } else {
            preview.visibility = View.GONE
            text.text = "No photo selected yet."
            text.setTextColor(colorRes(R.color.civic_muted))
        }
    }

    private fun updateLocationLabel() {
        reportLocationText?.text = selectedLocation?.let {
            "Lat: ${it.latitude.formatCoord()}\nLng: ${it.longitude.formatCoord()}"
        } ?: "Choose current location or select a point on the map"
    }

    private fun updateLocationModeButtons() {
        styleLocationModeButton(currentLocationButton, selectedLocationMode == LocationMode.CURRENT)
        styleLocationModeButton(mapLocationButton, selectedLocationMode == LocationMode.MAP)
    }

    private fun setSelectedCoordinates(latitude: Double, longitude: Double, mode: LocationMode) {
        selectedLocation = Location(mode.name.lowercase(Locale.US)).apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        selectedLocationMode = mode
        updateLocationLabel()
        updateLocationModeButtons()
    }

    private fun captureCurrentLocation(after: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        val location = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstNotNullOfOrNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }

        if (location == null) {
            toast("Location unavailable right now.")
        } else {
            setSelectedCoordinates(location.latitude, location.longitude, LocationMode.CURRENT)
        }
        after()
    }

    private fun setScreenContent(view: View, scrollable: Boolean) {
        if (activeTab != Screen.MAP) {
            osmMapView = null
            hideMapIssueSheet()
        }
        contentFrame.removeAllViews()
        (view.parent as? ViewGroup)?.removeView(view)
        if (scrollable) {
            contentFrame.addView(ScrollView(this).apply { addView(view) })
        } else {
            contentFrame.addView(view)
        }
    }

    private fun screenColumn(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16.dp(), 20.dp(), 16.dp(), 0)
    }

    private fun headlineBlock(title: String, subtitle: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        addView(TextView(this@MainActivity).apply {
            text = title
            setTextColor(colorRes(R.color.civic_ink))
            textSize = 30f
            setTypeface(typeface, Typeface.BOLD)
        })
        addView(TextView(this@MainActivity).apply {
            text = subtitle
            setTextColor(colorRes(R.color.civic_muted))
            textSize = 15f
            setLineSpacing(2f, 1f)
        }, matchWrap().apply {
            topMargin = 6.dp()
            bottomMargin = 18.dp()
        })
    }

    private fun searchRow(): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(searchPill(), LinearLayout.LayoutParams(matchParent(), 60.dp()))

        return row
    }

    private fun searchPill(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = roundStrokeBg(colorRes(R.color.civic_surface_low), colorRes(R.color.civic_outline), 18)
        setPadding(18.dp(), 0, 18.dp(), 0)
        addView(ImageView(this@MainActivity).apply {
            setImageResource(R.drawable.ic_search)
            setColorFilter(colorRes(R.color.civic_muted))
        }, LinearLayout.LayoutParams(20.dp(), 20.dp()).apply {
            rightMargin = 12.dp()
        })
        addView(TextView(this@MainActivity).apply {
            text = "Search community issues..."
            setTextColor(colorRes(R.color.civic_muted))
            textSize = 16f
        }, LinearLayout.LayoutParams(0, wrapContent(), 1f))
    }

    private fun filterChips(): View {
        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16.dp(), 0, 18.dp())
        }

        listOf("All Issues", "Infrastructure", "Safety", "Sanitation").forEach { label ->
            strip.addView(TextView(this).apply {
                text = label
                setTextColor(if (activeFilter == label) colorRes(R.color.white) else colorRes(R.color.civic_muted))
                textSize = 15f
                setPadding(18.dp(), 12.dp(), 18.dp(), 12.dp())
                background = roundBg(
                    if (activeFilter == label) colorRes(R.color.civic_primary_dark) else colorRes(R.color.civic_surface_low),
                    22
                )
                setOnClickListener {
                    activeFilter = label
                    showFeed()
                }
            }, LinearLayout.LayoutParams(wrapContent(), wrapContent()).apply {
                rightMargin = 10.dp()
            })
        }

        return HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(strip)
        }
    }

    private fun sectionLabel(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(colorRes(R.color.civic_ink))
        textSize = 17f
        setTypeface(typeface, Typeface.BOLD)
    }

    private fun fieldLabel(text: String): TextView = TextView(this).apply {
        this.text = text.uppercase(Locale.US)
        setTextColor(colorRes(R.color.civic_ink))
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
    }

    private fun locationModeButton(text: String, onClick: () -> Unit): MaterialButton = MaterialButton(this).apply {
        this.text = text
        textSize = 13f
        cornerRadius = 18.dp()
        insetTop = 0
        insetBottom = 0
        setOnClickListener { onClick() }
    }

    private fun styleLocationModeButton(button: MaterialButton?, selected: Boolean) {
        button ?: return
        button.setTextColor(if (selected) colorRes(R.color.white) else colorRes(R.color.civic_primary))
        button.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (selected) R.color.civic_primary else R.color.civic_surface_container
        )
    }

    private fun formInput(hint: String, singleLine: Boolean): EditText = EditText(this).apply {
        this.hint = hint
        this.isSingleLine = singleLine
        textSize = 16f
        minLines = if (singleLine) 1 else 4
        background = roundBg(colorRes(R.color.civic_surface_low), 14)
        setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
        setTextColor(colorRes(R.color.civic_ink))
        setHintTextColor(colorRes(R.color.civic_muted))
    }

    private fun elevatedCard(): MaterialCardView = MaterialCardView(this).apply {
        radius = 18.dp().toFloat()
        setCardBackgroundColor(colorRes(R.color.civic_card))
        strokeWidth = 1.dp()
        strokeColor = colorRes(R.color.civic_outline)
        cardElevation = 1.dp().toFloat()
        layoutParams = LinearLayout.LayoutParams(matchParent(), wrapContent()).apply {
            bottomMargin = 16.dp()
        }
    }

    private fun issueImage(issue: CivicIssue): ImageView = ImageView(this).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        if (issue.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(issue.imageUrl)
                .transform(CenterCrop(), RoundedCorners(18.dp()))
                .placeholder(issueImageRes(issue))
                .into(this)
        } else {
            setImageResource(issueImageRes(issue))
        }
    }

    private fun issueImageRes(issue: CivicIssue): Int {
        val category = issue.category.lowercase(Locale.US)
        return when {
            category.contains("light") -> R.drawable.issue_streetlight_lovable
            category.contains("sanitation") || category.contains("garbage") || category.contains("waste") -> R.drawable.issue_trash_lovable
            else -> R.drawable.issue_pothole_lovable
        }
    }

    private fun statusPill(status: String): TextView = TextView(this).apply {
        text = status
        textSize = 12f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(12.dp(), 8.dp(), 12.dp(), 8.dp())
        val bg = when {
            status.equals("Resolved", true) -> colorRes(R.color.civic_success_bg)
            status.equals("Assigned", true) || status.equals("In Progress", true) || status.equals("Under Review", true) || status.equals("Scheduled", true) -> colorRes(R.color.civic_pending_bg)
            else -> colorRes(R.color.civic_warning_bg)
        }
        val fg = when {
            status.equals("Resolved", true) -> colorRes(R.color.civic_success)
            status.equals("Assigned", true) || status.equals("In Progress", true) || status.equals("Under Review", true) || status.equals("Scheduled", true) -> colorRes(R.color.civic_primary)
            else -> colorRes(R.color.civic_warning)
        }
        setTextColor(fg)
        background = roundBg(bg, 18)
    }

    private fun priorityPill(priority: String): TextView = TextView(this).apply {
        text = if (priority.equals("High", true)) "HIGH PRIORITY" else "${priority.uppercase(Locale.US)} PRIORITY"
        textSize = 11f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(if (priority.equals("High", true)) colorRes(R.color.civic_error) else colorRes(R.color.civic_warning))
        setPadding(12.dp(), 8.dp(), 12.dp(), 8.dp())
        background = roundStrokeBg(
            if (priority.equals("High", true)) colorRes(R.color.civic_warning_bg) else colorRes(R.color.civic_warning_bg),
            if (priority.equals("High", true)) colorRes(R.color.civic_error) else colorRes(R.color.civic_warning),
            12
        )
    }

    private fun locationLine(issue: CivicIssue): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 10.dp(), 0, 10.dp())
        addView(ImageView(this@MainActivity).apply {
            setImageResource(R.drawable.ic_location_pin)
            setColorFilter(colorRes(R.color.civic_muted))
        }, LinearLayout.LayoutParams(16.dp(), 16.dp()).apply {
            rightMargin = 8.dp()
        })
        addView(TextView(this@MainActivity).apply {
            text = issueLocation(issue)
            setTextColor(colorRes(R.color.civic_muted))
            textSize = 14f
        })
    }

    private fun issueLocation(issue: CivicIssue): String {
        return when (issue.id) {
            "CT-1042" -> "Elm Street & 5th Avenue"
            "CT-1043" -> "Maple Avenue near Central Park"
            "CT-1044" -> "Main Street Market Block"
            else -> "${issue.latitude.formatCoord()}, ${issue.longitude.formatCoord()}"
        }
    }

    private fun issueAge(issue: CivicIssue): String {
        val createdAt = issue.createdAt?.toDate()
        if (createdAt != null) {
            val diff = System.currentTimeMillis() - createdAt.time
            val hours = diff / (1000L * 60L * 60L)
            if (hours < 24) return "$hours hours ago"
            val days = hours / 24
            return "$days days ago"
        }
        return when (issue.id) {
            "CT-1042" -> "2 hours ago"
            "CT-1043" -> "5 hours ago"
            "CT-1044" -> "Yesterday"
            else -> SimpleDateFormat("MMM d", Locale.US).format(Date())
        }
    }

    private fun bodyText(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(colorRes(R.color.civic_on_surface))
        textSize = 14f
        setLineSpacing(3f, 1f)
    }

    private fun metaText(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(colorRes(R.color.civic_muted))
        textSize = 13f
    }

    private fun metaAction(text: String, iconRes: Int): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(ImageView(this@MainActivity).apply {
            setImageResource(iconRes)
            setColorFilter(if (iconRes == R.drawable.ic_thumb) colorRes(R.color.civic_primary) else colorRes(R.color.civic_muted))
        }, LinearLayout.LayoutParams(16.dp(), 16.dp()).apply {
            rightMargin = 6.dp()
        })
        addView(metaText(text).apply {
            setTextColor(if (iconRes == R.drawable.ic_thumb) colorRes(R.color.civic_primary) else colorRes(R.color.civic_muted))
            textSize = 14f
        })
    }

    private fun divider(): View = View(this).apply {
        setBackgroundColor(colorRes(R.color.civic_outline))
        layoutParams = LinearLayout.LayoutParams(matchParent(), 1.dp()).apply {
            topMargin = 14.dp()
            bottomMargin = 14.dp()
        }
    }

    private fun timelineItem(title: String, body: String, active: Boolean): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            setPadding(0, 6.dp(), 0, 12.dp())
        }

        val icon = FrameLayout(this).apply {
            background = roundBg(
                if (active) colorRes(R.color.civic_secondary) else colorRes(R.color.civic_outline),
                12
            )
        }
        icon.addView(View(this).apply {
            background = roundBg(colorRes(R.color.white), 4)
        }, FrameLayout.LayoutParams(8.dp(), 8.dp(), Gravity.CENTER))
        row.addView(icon, LinearLayout.LayoutParams(24.dp(), 24.dp()).apply {
            rightMargin = 12.dp()
            topMargin = 4.dp()
        })

        val textColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        textColumn.addView(TextView(this).apply {
            text = title
            setTextColor(if (active) colorRes(R.color.civic_ink) else colorRes(R.color.civic_muted))
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
        })
        textColumn.addView(TextView(this).apply {
            text = body
            setTextColor(colorRes(R.color.civic_muted))
            textSize = 14f
            setLineSpacing(2f, 1f)
        })
        row.addView(textColumn, LinearLayout.LayoutParams(0, wrapContent(), 1f))
        return row
    }

    private fun detailRow(left: View, right: View): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        addView(left, LinearLayout.LayoutParams(0, wrapContent(), 1f).apply {
            rightMargin = 10.dp()
            bottomMargin = 10.dp()
        })
        addView(right, LinearLayout.LayoutParams(0, wrapContent(), 1f).apply {
            bottomMargin = 10.dp()
        })
    }

    private fun detailChip(label: String, value: String, valueColor: Int): MaterialCardView {
        val card = MaterialCardView(this).apply {
            radius = 14.dp().toFloat()
            cardElevation = 0f
            setCardBackgroundColor(colorRes(R.color.civic_surface_low))
            strokeWidth = 1.dp()
            strokeColor = colorRes(R.color.civic_surface_container)
        }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14.dp(), 12.dp(), 14.dp(), 12.dp())
        }
        column.addView(TextView(this).apply {
            text = label.uppercase(Locale.US)
            setTextColor(colorRes(R.color.civic_muted))
            textSize = 11f
        })
        column.addView(TextView(this).apply {
            text = value
            setTextColor(valueColor)
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
        }, matchWrap().apply {
            topMargin = 4.dp()
        })
        card.addView(column)
        return card
    }

    private fun mapMarker(color: Int, iconRes: Int): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        addView(FrameLayout(this@MainActivity).apply {
            background = roundBg(color, 20)
            addView(ImageView(this@MainActivity).apply {
                setImageResource(iconRes)
                setColorFilter(colorRes(R.color.white))
            }, FrameLayout.LayoutParams(22.dp(), 22.dp(), Gravity.CENTER))
        }, LinearLayout.LayoutParams(44.dp(), 44.dp()))
        addView(View(this@MainActivity).apply {
            background = roundBg(color, 2)
        }, LinearLayout.LayoutParams(4.dp(), 18.dp()))
    }

    private fun primaryButton(text: String, onClick: () -> Unit, marginBottom: Int = 16): MaterialButton {
        return MaterialButton(this).apply {
            this.text = text
            setTextColor(colorRes(R.color.white))
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
            cornerRadius = 18.dp()
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_primary_button)
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(matchParent(), 56.dp()).apply {
                bottomMargin = marginBottom.dp()
            }
        }
    }

    private fun ghostButton(text: String, onClick: () -> Unit): MaterialButton = MaterialButton(this).apply {
        this.text = text
        setTextColor(colorRes(R.color.civic_primary))
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
        cornerRadius = 18.dp()
        backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.civic_progress_bg)
        setOnClickListener { onClick() }
    }

    private fun compactAdminButton(text: String, destructive: Boolean, onClick: () -> Unit): MaterialButton {
        return MaterialButton(this).apply {
            this.text = text
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            minHeight = 0
            minimumHeight = 0
            insetTop = 0
            insetBottom = 0
            cornerRadius = 14.dp()
            setPadding(12.dp(), 0, 12.dp(), 0)
            setTextColor(
                if (destructive) colorRes(R.color.civic_error) else colorRes(R.color.civic_primary)
            )
            backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.civic_progress_bg)
            setOnClickListener { onClick() }
        }
    }

    private fun ghostIconButton(iconRes: Int, onClick: () -> Unit): FrameLayout = FrameLayout(this).apply {
        background = roundStrokeBg(colorRes(R.color.civic_card), colorRes(R.color.civic_outline), 16)
        setOnClickListener { onClick() }
        addView(ImageView(this@MainActivity).apply {
            setImageResource(iconRes)
            setColorFilter(colorRes(R.color.civic_muted))
        }, FrameLayout.LayoutParams(22.dp(), 22.dp(), Gravity.CENTER))
    }

    private fun centerBody(text: String): TextView = TextView(this).apply {
        this.text = text
        gravity = Gravity.CENTER
        setTextColor(colorRes(R.color.civic_muted))
        textSize = 14f
        setLineSpacing(3f, 1f)
    }

    private fun bottomSpacer(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(matchParent(), 92.dp())
    }

    private fun roundBg(fill: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = radiusDp.dp().toFloat()
        }

    private fun roundStrokeBg(fill: Int, stroke: Int, radiusDp: Int): GradientDrawable =
        roundBg(fill, radiusDp).apply {
            setStroke(1.dp(), stroke)
        }

    private fun roundDashedBg(fill: Int, stroke: Int, radiusDp: Int): GradientDrawable =
        roundBg(fill, radiusDp).apply {
            setStroke(2.dp(), stroke, 14.dp().toFloat(), 10.dp().toFloat())
        }

    private fun matchWrap() = LinearLayout.LayoutParams(matchParent(), wrapContent())

    private fun colorRes(color: Int) = ContextCompat.getColor(this, color)

    private fun matchParent() = ViewGroup.LayoutParams.MATCH_PARENT

    private fun wrapContent() = ViewGroup.LayoutParams.WRAP_CONTENT

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    private fun Double.formatCoord(): String = String.format(Locale.US, "%.5f", this)

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private enum class Screen {
        FEED,
        MAP,
        REPORT,
        MY_ISSUES,
        DETAILS
    }

    private enum class LocationMode {
        NONE,
        CURRENT,
        MAP
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 201
        private val DEFAULT_CITY = GeoPoint(18.5204, 73.8567)
        private const val DEFAULT_CITY_ZOOM = 13f
    }
}
