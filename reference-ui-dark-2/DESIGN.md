---
name: CivicTrack Modern
colors:
  surface: '#0f1417'
  surface-dim: '#0f1417'
  surface-bright: '#353a3d'
  surface-container-lowest: '#0a0f12'
  surface-container-low: '#171c1f'
  surface-container: '#1b2023'
  surface-container-high: '#262b2e'
  surface-container-highest: '#303539'
  on-surface: '#dfe3e7'
  on-surface-variant: '#bdc8d0'
  inverse-surface: '#dfe3e7'
  inverse-on-surface: '#2c3134'
  outline: '#889299'
  outline-variant: '#3e484f'
  surface-tint: '#75d1ff'
  primary: '#9adbff'
  on-primary: '#003548'
  primary-container: '#4fc3f7'
  on-primary-container: '#004e69'
  inverse-primary: '#006688'
  secondary: '#9ecaff'
  on-secondary: '#003258'
  secondary-container: '#1e95f2'
  on-secondary-container: '#002b4d'
  tertiary: '#ffc893'
  on-tertiary: '#4a2800'
  tertiary-container: '#faa443'
  on-tertiary-container: '#6b3d00'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c2e8ff'
  primary-fixed-dim: '#75d1ff'
  on-primary-fixed: '#001e2b'
  on-primary-fixed-variant: '#004d67'
  secondary-fixed: '#d1e4ff'
  secondary-fixed-dim: '#9ecaff'
  on-secondary-fixed: '#001d36'
  on-secondary-fixed-variant: '#00497d'
  tertiary-fixed: '#ffdcbd'
  tertiary-fixed-dim: '#ffb86f'
  on-tertiary-fixed: '#2c1600'
  on-tertiary-fixed-variant: '#693c00'
  background: '#0f1417'
  on-background: '#dfe3e7'
  surface-variant: '#303539'
typography:
  h1:
    fontFamily: Public Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  h2:
    fontFamily: Public Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.01em
  h3:
    fontFamily: Public Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
  body-lg:
    fontFamily: Public Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Public Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: '1.4'
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1.4'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 16px
  margin: 24px
---

## Brand & Style

The design system is built upon a foundation of **Civic Intelligence**. It aims to transform complex municipal data into an actionable, high-clarity interface that feels both institutional and cutting-edge. The brand personality is authoritative yet accessible, mirroring the reliability of a government utility with the sleek efficiency of modern mobility platforms.

The visual style blends **Modern Corporate** aesthetics with **Glassmorphism**. It utilizes high-contrast text and vibrant accents against a deep, multi-layered dark canvas to ensure legibility during night-time field use. The emotional response should be one of "controlled urgency"—providing users with the confidence to navigate urban data with precision and speed.

## Colors

The palette is optimized for a **Dark Mode** environment, drawing inspiration from high-end navigation systems. 

- **Foundation:** The background uses a deep obsidian blue to reduce eye strain, while tiered surfaces create a sense of physical depth.
- **Action & Identity:** The Primary Light Blue is reserved for interactive elements and highlights, while the Accent Blue provides a deeper tonal anchor for gradients and primary actions.
- **Semantic Logic:** Success, Warning, and Error colors are tuned for high luminosity against dark backgrounds, ensuring critical status updates are never missed.
- **Mapping:** Markers on map interfaces must use the pure semantic values (Red, Green, Blue) to maintain maximum contrast against the dark-themed base map.

## Typography

This design system utilizes **Public Sans** as its primary typeface to evoke an institutional, trustworthy, and official character. For technical data and UI labels, **Inter** is employed due to its systematic and utilitarian clarity.

**Hierarchy is established through contrast rather than size alone:**
- **Headings:** Bold, "white-ish" (`#E6EDF3`) and tightly tracked to feel impactful.
- **Body:** Slightly dimmed (`#9BA1A6`) to ensure that the primary information and headlines remain the focal point of the layout.
- **Utility Text:** Caps and semi-bold weights are used for labels to differentiate interactive metadata from narrative content.

## Layout & Spacing

The design system employs a **12-column fluid grid** with fixed margins for desktop and a flexible 4-column grid for mobile devices. The rhythm is governed by an **8px base unit**, ensuring consistent alignment across all components.

- **Margins:** 24px default for edge-to-edge containers.
- **Gutter:** 16px to allow for breathable separation between cards.
- **Density:** High information density is encouraged for data views, using 4px and 8px increments to cluster related information tightly within their respective surfaces.

## Elevation & Depth

Hierarchy is communicated through a combination of **Tonal Layering** and **Glassmorphism**:

1.  **Level 0 (Base):** `#0D1117` - The canvas.
2.  **Level 1 (Card/Surface):** `#161B22` - Used for primary content containers. These should feature a 1px subtle border (`#30363D`) to define edges.
3.  **Level 2 (Elevated):** `#1F2630` - Used for modals, dropdowns, and hovered states.
4.  **Glassmorphism Effect:** Overlays and sidebars should use a semi-transparent version of the surface color (80% opacity) with a `backdrop-filter: blur(12px)`. This maintains context while focusing the user on the foreground.
5.  **Shadows:** Shadows are minimal and diffused, using a dark `#000000` at 40% opacity with a large blur (16-24px) to suggest soft elevation without harsh lines.

## Shapes

The shape language is modern and approachable. The standard radius for cards, containers, and large buttons is **16px** (1rem). 

- **Cards/Modals:** 16px corner radius.
- **Badges/Chips:** Pill-shaped (fully rounded) to contrast against the structured grid.
- **Input Fields:** 8px (Soft) radius to maintain a more functional, "tool-like" appearance compared to decorative cards.

## Components

### Buttons
Primary buttons feature a linear gradient from **Accent Blue (#2196F3)** to a deeper **Navy (#0D47A1)**. They include a subtle outer glow of the same color on hover. Text is bold and white. Secondary buttons use a ghost style with a 1px border.

### Badges & Chips
Status indicators must be **Pill-shaped**. They utilize a soft glowing background (15% opacity of the semantic color) with high-contrast text and a subtle 2px solid marker dot of the status color for accessibility.

### Cards
Cards are the primary organizational unit. They should use the `Surface` color, a `16px` radius, and a `1px` border. In specific dashboard views, cards may use the glassmorphism blur effect to allow map details to peek through the UI.

### Input Fields
Inputs use the `Surface Elevated` color to contrast against the `Surface` background. Focus states are indicated by a `2px` solid `Primary Light Blue` border and a soft glow.

### Maps & Markers
The map component is the core of the system. It uses a custom dark theme (Uber-style) where geography is rendered in shades of grey and black. Map markers are high-luminosity circular pips with a white outer ring to ensure they pop against the dark landscape.