# AgentHub Site Structure

# Overview

AgentHub is an AI-based GitHub OpenSource Follow-up Platform.

The platform helps developers:

- Discover trending AI open source repositories
- Understand repositories quickly using AI analysis
- Track repository updates and trends
- Discuss repositories with other developers

Core Flow:

```text
Discover → Analyze → Follow-up → Discuss
```

---

# Full Site Structure

```text
Home
 ├─ Hero Section
 ├─ Weekly Trending Repositories
 ├─ Popular Topics
 ├─ Weekly Radar Report Preview
 ├─ Recently Exploding Repositories
 ├─ Popular Discussions
 └─ Search

Trending Repositories
 ├─ Repository List
 ├─ Topic Filter
 ├─ Language Filter
 ├─ Time Range Filter
 ├─ Trend Score Sort
 ├─ Star Growth Sort
 ├─ Search
 └─ Repository Detail
      ├─ Repository Header
      ├─ Core Metrics
      ├─ AI Summary
      ├─ Architecture Analysis
      ├─ Tech Stack Analysis
      ├─ Risk Analysis
      ├─ File Structure
      ├─ Trend Graph
      ├─ Similar Repositories
      └─ Repository Discussion

Weekly Radar Report
 ├─ Weekly Trends
 ├─ Top Repositories
 ├─ Most Discussed Technologies
 ├─ MCP Trends
 ├─ Agent Trends
 ├─ AI Tool Trends
 └─ Recommended Repositories

Community
 ├─ Free Board
 │   ├─ Post List
 │   ├─ Post Detail
 │   ├─ Create Post
 │   ├─ Edit Post
 │   ├─ Delete Post
 │   ├─ Like Post
 │   └─ Comment
 └─ Search & Sort

Repository Discussion
 ├─ Repository Header
 ├─ Discussion List
 ├─ Discussion Detail
 ├─ Create Discussion
 ├─ Edit Discussion
 ├─ Delete Discussion
 ├─ Comment
 └─ Reply

Notifications
 ├─ Repository Star Increase Notification
 ├─ Release Update Notification
 ├─ README Update Notification
 ├─ Trending Score Notification
 ├─ Discussion Activity Notification
 └─ Bookmark Repository Notifications

My Page
 ├─ Bookmarked Repositories
 ├─ Notification Settings
 ├─ My Posts
 ├─ My Comments
 ├─ Account Settings
 └─ Activity History

Authentication
 ├─ Login
 │   ├─ GitHub OAuth
 │   ├─ Google OAuth
 │   └─ Email Login
 ├─ Signup
 ├─ Logout
 └─ JWT Token Refresh

Admin
 ├─ Repository Collection Status
 ├─ Failed Collection Logs
 ├─ User Management
 ├─ Community Moderation
 ├─ Report Management
 ├─ AI Execution Logs
 └─ Notification Management
```

---

# Page Detail Description

# 1. Home

## Purpose

Main landing page for discovering trending AI repositories.

## Main Features

```text
- Weekly Trending Repository Ranking
- AI/Agent Popular Topics
- Search Bar
- Weekly Radar Report Preview
- Recently Exploding OpenSource
- Popular Discussions
```

## Main Components

```text
Navbar
Hero Section
Topic Chips
Repository Cards
Trend Graph
Discussion Cards
Footer
```

---

# 2. Trending Repositories

## Purpose

Allow users to efficiently explore trending repositories.

## Main Features

```text
- Trend Score Ranking
- Star Growth Ranking
- Topic Filter
- Language Filter
- Time Range Filter
- Search
- Bookmark
```

## Repository Card Information

```text
Repository Name
Description
Topics
Programming Language
Star Count
Fork Count
Trend Score
AI Summary Preview
Bookmark Button
```

---

# 3. Repository Detail Page

## Purpose

Help users understand repositories quickly without reading the full README.

## Main Sections

---

## 3-1. Repository Header

### Information

```text
Repository Name
Owner
GitHub Link
Bookmark Button
Notification Toggle
```

---

## 3-2. Core Metrics

### Metrics

```text
Stars
Forks
Issues
Trend Score
Star Growth
Recent Update
```

### UI Style

```text
Dashboard Metric Cards
```

---

## 3-3. AI Summary

### Purpose

Provide an instant understanding of the repository.

### Summary Examples

```text
- Project Purpose
- Core Features
- Workflow Structure
- Recommended Usage
- Target Developers
```

---

## 3-4. Architecture Analysis

### Analysis Targets

```text
Agent Flow
Workflow Structure
Memory Architecture
Tool Calling Structure
Pipeline Design
```

### UI Recommendation

```text
Flow Diagram
Node-style Cards
Architecture Blocks
```

---

## 3-5. Tech Stack Analysis

### Example

```text
FastAPI
LangGraph
Redis
PostgreSQL
OpenAI SDK
Docker
```

### UI Style

```text
Badge UI
```

---

## 3-6. Risk Analysis

### Examples

```text
Low Maintenance
High Issue Count
Deprecated Possibility
Inactive Commit Activity
```

### UI Style

```text
Warning Cards
Status Badges
```

---

## 3-7. File Structure

### Example

```text
/src
/agents
/tools
/workflows
/prompts
```

### UI Style

```text
Tree Explorer UI
```

---

## 3-8. Trend Graph

### Visualize

```text
Star Growth
Trend Score Changes
Discussion Growth
Bookmark Growth
```

### Recommended UI

```text
Minimal Line Charts
```

---

## 3-9. Similar Repositories

### Purpose

Recommend repositories with similar topics or architecture.

### Recommendation 기준

```text
- Similar Topics
- Similar Tech Stack
- Similar Workflow
- Similar AI Architecture
```

---

## 3-10. Repository Discussion

### Purpose

Repository-specific communication space.

### Features

```text
Discussion Posts
Comments
Replies
Likes
Sorting
Search
```

---

# 4. Weekly Radar Report

## Purpose

Provide weekly AI/OpenSource trend analysis.

## Main Sections

```text
Weekly Trends
Top Repositories
Most Discussed Technologies
MCP Trends
Agent Trends
Recommended Repositories
```

## Style Direction

```text
Notion + Medium Style
```

---

# 5. Community

## Purpose

General developer communication space.

## Main Features

```text
Free Board
Questions
Discussions
Information Sharing
Reviews
```

## Community Features

```text
Post CRUD
Comment CRUD
Like
Search
Sort
Pagination
```

---

# 6. Notifications

## Purpose

Track important repository updates.

## Notification Examples

```text
"LangGraph stars increased by +1200"

"New release published"

"README updated"

"Trending discussion increased"
```

## Notification Types

```text
Repository Update
Release Notification
Discussion Notification
Bookmark Repository Notification
Trend Notification
```

---

# 7. My Page

## Purpose

Manage user activities and preferences.

## Sections

```text
Bookmarked Repositories
Notification Settings
My Posts
My Comments
Activity History
Account Settings
```

---

# 8. Authentication

## Recommended Authentication

```text
GitHub OAuth
Google OAuth
Email Login
```

GitHub OAuth should be prioritized because the target users are developers.

---

# 9. Admin Page

## Purpose

Manage platform operation and monitoring.

## Main Features

```text
Repository Collection Status
Failed Collection Logs
User Management
Community Moderation
Report Management
AI Execution Logs
Notification Management
```

---

# UX Priority

The most important page is:

```text
Repository Detail Page
```

Because the core value of AgentHub is:

```text
Quickly understanding and following rapidly changing AI open source repositories.
```

---

# UX Principles

Always prioritize:

```text
Readability
Fast Understanding
Technical Clarity
Information Hierarchy
```

Avoid:

```text
Visual Clutter
Heavy Animation
SNS-style Layout
Overly Colorful Design
```

---

# Final Product Direction

AgentHub should feel like:

```text
A modern AI developer intelligence dashboard
inspired by TailwindCSS, Vercel, Linear, and GitHub.
```