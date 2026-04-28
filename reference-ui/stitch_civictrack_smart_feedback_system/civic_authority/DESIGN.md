---
name: Civic Authority
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#43474e'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#74777f'
  outline-variant: '#c4c6cf'
  surface-tint: '#455f88'
  primary: '#002045'
  on-primary: '#ffffff'
  primary-container: '#1a365d'
  on-primary-container: '#86a0cd'
  inverse-primary: '#adc7f7'
  secondary: '#006c4a'
  on-secondary: '#ffffff'
  secondary-container: '#82f5c1'
  on-secondary-container: '#00714e'
  tertiary: '#002339'
  on-tertiary: '#ffffff'
  tertiary-container: '#003959'
  on-tertiary-container: '#45a6eb'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d6e3ff'
  primary-fixed-dim: '#adc7f7'
  on-primary-fixed: '#001b3c'
  on-primary-fixed-variant: '#2d476f'
  secondary-fixed: '#85f8c4'
  secondary-fixed-dim: '#68dba9'
  on-secondary-fixed: '#002114'
  on-secondary-fixed-variant: '#005137'
  tertiary-fixed: '#cce5ff'
  tertiary-fixed-dim: '#93ccff'
  on-tertiary-fixed: '#001d31'
  on-tertiary-fixed-variant: '#004b73'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Public Sans
    fontSize: 57px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Public Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Public Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-lg:
    fontFamily: Public Sans
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Public Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Public Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-lg:
    fontFamily: Public Sans
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Public Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  margin-mobile: 16px
  margin-tablet: 24px
  gutter: 16px
  stack-sm: 4px
  stack-md: 12px
  stack-lg: 24px
---

## Brand & Style

The brand personality is rooted in transparency, reliability, and civic duty. This design system adopts a **Corporate / Modern** aesthetic that adheres strictly to Material Design 3 (MD3) principles to ensure familiarity and ease of use for a broad demographic. The visual language conveys a sense of institutional stability while maintaining the efficiency of a modern digital tool. 

The UI prioritizes clarity and high contrast to ensure accessibility for all citizens. It utilizes purposeful whitespace and a structured information hierarchy to transform complex civic data into actionable insights. The emotional response should be one of empowerment and trust—users should feel that their contributions are heard and that the platform is a dependable bridge to their community leaders.

## Colors

The color palette is built on a foundation of "Deep Blue" to evoke professionalism and "Emerald Green" to signal successful outcomes. 

- **Primary:** A deep, authoritative navy used for headers, primary actions, and branding elements to establish a sense of security.
- **Secondary:** An emerald green utilized for success states, "Resolved" indicators, and secondary calls to action that imply positive movement.
- **Tertiary:** A bright sky blue used for "In Progress" states and informational highlights.
- **Surface & Background:** The system uses a neutral-cool scale for backgrounds to keep the interface feeling airy and modern. Surfaces follow MD3 tonal palettes, where higher elevation is represented by lighter tones in light mode.
- **Semantic Colors:** Status-specific colors are calibrated for high legibility against white backgrounds, meeting WCAG AA standards.

## Typography

This design system utilizes **Public Sans** for all levels of the hierarchy. Originally designed for government use, it offers exceptional clarity and a neutral, institutional tone that fits the civic context perfectly.

- **Headlines:** Use semi-bold weights to provide a strong anchor for page sections. 
- **Body Text:** Letter spacing is slightly increased in body styles to improve legibility on mobile screens, especially for longer descriptions of civic issues.
- **Labels:** Used for status badges and metadata, labels use a medium weight to ensure they remain distinct from surrounding body text at smaller sizes.

## Layout & Spacing

The system employs a **fluid grid** based on an 8dp square module, ensuring consistency with Material Design 3 standards.

- **Grid:** A 4-column grid is used for mobile devices, expanding to 8 columns for tablets and 12 for desktop layouts.
- **Margins:** 16dp margins are the standard for mobile to maximize content area while maintaining a clean frame.
- **Rhythm:** Vertical spacing follows a strict 8dp increment rule. Use "stack-sm" for related elements (like a label and an input), "stack-md" for components within a card, and "stack-lg" for separating major sections of a page.

## Elevation & Depth

In alignment with modern Android standards, this design system primarily uses **Tonal Layers** rather than heavy shadows to convey depth.

- **Surfaces:** Different levels of elevation are represented by varying color intensities of the primary tint over the surface color. For example, a card at Level 1 has a subtle primary tint, while a Floating Action Button (FAB) at Level 3 appears more saturated or has a soft, ambient shadow.
- **Shadows:** When used, shadows are highly diffused and low-opacity (10-15%), serving as a secondary cue for interactivity rather than a primary structural element.
- **Dividers:** Use low-contrast outlines (1px solid, 10% opacity) for list items to maintain a flat, clean appearance while providing necessary separation.

## Shapes

The design system utilizes **Rounded** shapes (Level 2) to strike a balance between a friendly, modern app and a serious, official tool.

- **Standard Elements:** Buttons and input fields use a 0.5rem (8px) corner radius.
- **Cards:** Major containers and surface cards use a 1rem (16px) radius to create a soft, contained look for grouped information.
- **Status Badges:** Small badges and chips use a pill-shaped (fully rounded) geometry to distinguish them from interactive buttons.

## Components

### Buttons
- **Primary:** Filled with the primary deep blue; used for the main action (e.g., "Submit Report").
- **Secondary:** Outlined with a 1px border; used for auxiliary actions (e.g., "Save Draft").
- **FAB:** Uses a large, rounded-xl shape with a tertiary color for high visibility on the dashboard.

### Status Badges & Priority
- **Status Badges:** Compact containers with a subtle background tint and high-contrast text. 
    - *Pending:* Amber background, dark amber text.
    - *In Progress:* Light blue background, deep blue text.
    - *Resolved:* Emerald green background, white or dark green text.
- **Priority Indicators:** Represented by a colored vertical bar on the left edge of cards. Red for "High," Orange for "Medium," and Gray for "Low."

### Cards
- Cards are used to group report details. They feature a 1px soft border or a Level 1 tonal elevation. Content inside cards follows a consistent 16dp internal padding.

### Input Fields
- MD3-style "Filled" text fields with a bottom-line indicator. This style is preferred for mobile as it provides a larger touch target and clearer visibility in varied lighting conditions.

### Lists
- Clean, edge-to-edge list items with 16dp horizontal padding. Each item uses a high-contrast title and a secondary-level body text for the description.