# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a legacy Meteor.js music release catalog application called "apogsasis-meteor" written primarily in CoffeeScript. The app displays music releases, tracks, videos, and artist information with a responsive web interface.

**Project Goal:** Port this legacy application to [Stasis](https://github.com/magnars/stasis), a Clojure static site generator, for deployment to GitHub Pages. The port will use:
- tools.deps for dependency management
- Latest Clojure version
- EDN files for music release input data (replacing MongoDB collections)
- Hiccup for HTML templating (replacing Meteor templates)
- Static HTML generation (removing all interactive server components)
- GitHub Pages deployment (replacing custom server deployment)

## Development Commands

**Install dependencies:**
```bash
npm install -g meteorite
```

**Run development server:**
```bash
cd apogsasis-meteor
mrt
```

**Build and deploy:**
```bash
cd apogsasis-meteor
./deploy.sh
```

The deploy script builds the app, creates a bundle, and deploys to a remote server.

## Architecture

**File Structure:**
- `client/` - Frontend code (CoffeeScript, HTML templates, CSS)
- `server/` - Backend server code (CoffeeScript)
- `collections/` - Meteor collection definitions (models.coffee)
- `public/` - Static assets (images, fonts, audio files)
- `routes.coffee` - Client-side routing configuration

**Key Collections:**
- `Releases` - Music release data with tracks and metadata
- `Artists` - Artist information  
- `Videos` - Video content
- `Activities` - User activity tracking

**Technology Stack:**
- Meteor.js framework (legacy version using Meteorite)
- CoffeeScript for all logic
- Bootstrap CSS framework
- jQuery for DOM manipulation
- MongoDB for data storage

**Client Architecture:**
- Template-based views in `client/views/` directories
- Each view has `.coffee` (logic) and `.html` (template) files  
- Session-based state management for UI state
- Router handles `/releases/:id` and `/videos/:id` routes

**Data Flow:**
- Server seeds data from hardcoded release information
- Publications/subscriptions sync data to client
- Templates reactively update based on Session variables
- All collections are read-only (insert/update/remove: false)

## Key Files

- `src/apogsasis/core.clj` - Main application logic with Hiccup templates
- `src/apogsasis/build.clj` - Build script for static site generation
- `src/apogsasis/dev.clj` - Development server with live reload
- `resources/data/releases.edn` - Music release data (converted from MongoDB)
- `resources/data/videos.edn` - Video data with Vimeo integration
- `resources/public/` - Static assets (CSS, images, fonts)

## Development Guidelines

**⚠️ CRITICAL: Code Quality Requirements**
- **ALWAYS ensure balanced parentheses/brackets** - Unmatched delimiters will break the build
- **NO trailing whitespace** after code lines
- **NO lines containing only whitespace** - use completely empty lines instead
- **Validate syntax** with `clj -M:build` before committing
- Use proper Clojure formatting and indentation

## Current Theme

The application now uses [WebTUI CSS](https://webtui.ironclad.sh/) for a clean, terminal-style interface:
- Loaded via CDN: `https://cdn.jsdelivr.net/npm/@webtui/css@latest/dist/full.css`
- Monospace typography with professional terminal aesthetic
- CSS variables for theming (`--border`, `--surface`, etc.)
- Responsive grid layouts for video galleries
- Clean sidebar navigation with active states