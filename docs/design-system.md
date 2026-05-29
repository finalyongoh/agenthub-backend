AgentHub UI/UX Design Guide (TailwindCSS Inspired Edition)

Design Vision

AgentHub should feel like:

The TailwindCSS ecosystem meets GitHub intelligence.

The overall visual direction should combine:

TailwindCSS.com
Vercel
Linear
GitHub
Perplexity

The UI should prioritize:

Sharp typography
Modern gradients
Clear spacing system
Minimal but expressive visuals
Developer-focused readability
Structured content hierarchy

⸻

Core Design Philosophy

1. Minimal Surface Noise

Use:

Soft borders
Subtle background layers
Minimal shadows
Low-contrast separators

Avoid:

Heavy cards
Thick borders
Strong neumorphism
Overly bright gradients

⸻

2. Typography First

Like TailwindCSS.com, typography should carry the experience.

Use:

Large bold headlines
Strong contrast
Readable line-height
Spacious section spacing
Compact supporting UI

Typography hierarchy should feel intentional and premium.

⸻

3. Structured Developer UI

The product is information-heavy.

Design should optimize:

Fast scanning
Repository comparison
Dashboard readability
Code-friendly visualization

Every section should immediately communicate:

What
Why
Current status
Importance

⸻

Visual Design Direction

Background System

Inspired by TailwindCSS dark mode.

Primary Background

#020617

Secondary Background

#0F172A

Elevated Surface

#111827
#1E293B

Border Color

rgba(148,163,184,0.12)

⸻

Accent Color System

Primary Accent

#38BDF8

Secondary Accent

#8B5CF6

Gradient Accent

from-sky-400 via-cyan-300 to-blue-500

Used for:

Hero highlights
Buttons
Graph highlights
Focused metrics
Important badges

⸻

Text System

Primary Text

#F8FAFC

Secondary Text

#CBD5E1

Muted Text

#64748B

⸻

Status Colors

Success

#22C55E

Warning

#F59E0B

Danger

#EF4444

Info

#38BDF8

⸻

Typography

Recommended Fonts

Inter
Pretendard
Geist

⸻

Heading Style

Hero Heading

font-weight: 800
letter-spacing: -0.04em
line-height: 1

Style reference:

TailwindCSS hero typography

⸻

Section Heading

Use:

Bold
Compact
High contrast

Examples:

Trending Repositories
Repository Intelligence
AI Architecture Analysis

⸻

Paragraph Rules

Use:

Short paragraphs
Clear spacing
Readable line width

Recommended:

max-width: 70ch

⸻

Layout System

Overall Layout

Recommended:

Sidebar + Content + Optional Right Panel

Structure:

┌──────────┬────────────────────┬─────────────┐
│ Sidebar  │ Main Content       │ Right Panel │
└──────────┴────────────────────┴─────────────┘

⸻

Spacing Philosophy

Inspired by TailwindCSS section rhythm.

Recommended Section Padding

Desktop: py-24
Tablet: py-20
Mobile: py-16

Content Width

max-w-7xl

⸻

Navigation Design

Navbar Style

Design Direction

Sticky
Transparent blur
Thin border-bottom
Minimal navigation

Recommended Style

backdrop-blur-xl
bg-slate-950/70
border-white/10

⸻

Navbar Contents

Logo
Trending
Radar
Community
Discussions
Notifications
Search
Profile Menu

⸻

Sidebar Design

Sidebar should feel like:

GitHub + Linear

Use:

Compact navigation
Small icons
Muted labels
Hover glow

Avoid:

Large colorful sidebar
Heavy active states

⸻

Hero Section Design

Main Goal

Users should instantly understand:

This platform helps track AI open source trends.

⸻

Hero Layout

Headline
Description
Search Bar
Popular Topics
Live Trending Preview

⸻

Hero Headline Style

Example tone:

Discover the fastest-growing AI repositories.

Typography style:

Very large
Bold
Tight letter spacing
Gradient emphasis

Example visual direction:

Track the future of AI open source.

Where:

future of AI

uses gradient text.

⸻

Hero Background

Use subtle:

Grid patterns
Noise texture
Blurred gradients
Glow effects

Avoid:

Heavy illustrations
3D artwork
Large mascots

⸻

Search UI Design

Search is a primary interaction.

Style inspiration:

TailwindCSS search
Raycast
Perplexity

⸻

Search Input Style

rounded-2xl
border-white/10
bg-slate-900/70
backdrop-blur

Use:

Large height
Clear placeholder
Command palette 느낌

⸻

Repository Card Design

Most Important List Component

Cards should feel:

Dense
Technical
Modern
Fast to scan

⸻

Card Style

Recommended

rounded-2xl
border border-white/10
bg-slate-900/60
hover:bg-slate-900
transition-all

⸻

Card Layout

Repository Name
Description
Topics
Metrics
AI Summary
Footer Actions

⸻

Repository Name Style

Use:

Large semi-bold
High contrast

Add:

Owner/repository

style similar to GitHub.

⸻

Topic Chips

Tailwind-inspired chip design:

rounded-full
bg-sky-500/10
text-sky-300
border border-sky-500/20

Small and compact.

⸻

Metrics UI

Metrics should look like:

Tiny dashboard indicators

Recommended:

Stars
Forks
Trend Score
Weekly Growth

Use:

Monospace numbers
Small labels

⸻

Repository Detail Page

Highest Priority Screen

This page should feel like:

Developer intelligence dashboard

The page should communicate:

What this repository does
How it works
Whether it matters
Whether it is growing

within seconds.

⸻

Detail Page Layout

Recommended Structure

Header
Metrics Row
AI Summary
Architecture Analysis
Tech Stack
Trend Charts
Discussions
Similar Repositories

⸻

Repository Header

Layout

Repository Info Left
Action Buttons Right

⸻

Action Buttons

Style inspiration:

TailwindCSS buttons
Vercel dashboard

Button style:

rounded-xl
border border-white/10
bg-white/5
hover:bg-white/10

⸻

Metrics Cards

Metrics should use:

Minimal dashboard cards

Design:

rounded-2xl
border border-white/10
bg-slate-900/50

Inside:

Small label
Large metric number
Tiny growth indicator

⸻

AI Summary Section

Main UX Highlight

This section should visually stand out.

Use:

Larger spacing
Readable typography
Highlighted border glow

Recommended style:

bg-gradient-to-b
from-sky-500/5
to-transparent

⸻

Summary Design Goal

Users should understand:

Purpose
Core functionality
Architecture direction
Key technologies

within 10 seconds.

⸻

Architecture Analysis UI

Visual Style

Should resemble:

Tailwind UI diagrams
Vercel architecture docs

⸻

Recommended Components

Flow nodes
Connection lines
Pipeline blocks
Agent chain diagrams

Use:

Minimal gradients
Thin lines
Soft glow

⸻

Tech Stack UI

Use badge-based layout.

Example:

FastAPI
LangGraph
Redis
PostgreSQL
OpenAI SDK

Badge style:

rounded-lg
border border-white/10
bg-slate-800
px-3 py-1

⸻

Risk Analysis Design

Risk UI should feel:

Clear
Subtle
Informative

Avoid:

Aggressive red warning blocks

Instead use:

Muted amber indicators

⸻

File Structure UI

Style Inspiration

VSCode explorer
GitHub tree view

Use:

Monospace font
Tree indentation
Compact spacing

⸻

Trend Graph Design

Visual Style

Inspired by:

Vercel analytics
Linear charts

Recommended:

Simple line charts
Thin grid lines
Minimal labels
Gradient fills

Avoid:

Complex chart UI
Pie charts
Heavy legends

⸻

Discussion UI

Style Direction

GitHub Discussion + Linear comments

Use:

Compact comment cards
Soft separators
Readable spacing

⸻

Weekly Radar Report Design

Style Inspiration

Tailwind blog
Notion
Medium

The reading experience should feel premium.

⸻

Recommended Layout

Large title
Summary block
Trend sections
Repository highlights
Charts
Insights

⸻

Notification UI

Notifications should feel:

Real-time
Developer focused
Actionable

⸻

Notification Card Style

rounded-xl
border border-white/10
bg-slate-900/60

Include:

Repository icon
Update summary
Time
Importance level

⸻

Login / Signup Design

Style Direction

Minimal authentication screen.

Inspired by:

Vercel
Linear
Tailwind UI

⸻

Authentication Layout

Centered card
Minimal form
Strong typography
OAuth-first

GitHub OAuth should be visually prioritized.

⸻

Mobile Responsive Strategy

Mobile Philosophy

Even on mobile:

Technical readability first

Avoid:

Oversized cards
Huge spacing
Massive hero sections

⸻

Mobile Layout Rules

Mobile Navigation

Use:

Bottom navigation
Drawer sidebar
Compact search

⸻

Mobile Repository Cards

Prioritize:

Repository name
Trend score
AI summary

Hide lower-priority metadata.

⸻

Motion & Interaction

Animation Philosophy

TailwindCSS-inspired motion:

Fast
Subtle
Purposeful

⸻

Recommended Animations

Fade-in
Hover glow
Border highlight
Tiny upward hover movement
Smooth opacity transitions

Avoid:

Heavy parallax
Bouncy animations
Large-scale transitions

⸻

Shadow System

Use soft shadows only.

Recommended:

shadow-[0_0_0_1px_rgba(255,255,255,0.04)]
shadow-lg shadow-sky-500/5

⸻

Border Radius System

Recommended Radius

Cards: rounded-2xl
Buttons: rounded-xl
Inputs: rounded-2xl
Badges: rounded-full

⸻

Design System Keywords

Clean
Sharp
Technical
Modern
Dense
Readable
Premium Developer Experience

⸻

Avoid These Styles

Large colorful gradients everywhere
Overly playful UI
NFT/Web3 aesthetics
Glassmorphism overload
Huge empty spacing
Heavy drop shadows
SNS-style feeds

⸻

Final Product Feeling

AgentHub should feel like:

The Bloomberg Terminal for AI Open Source.

Combined with:

TailwindCSS visual quality
GitHub technical familiarity
Linear interaction polish
Vercel dashboard clarity

The experience should communicate:

Professional
Fast
Technical
Trustworthy
Developer-first