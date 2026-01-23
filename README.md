# AgenceVoyageMultiAgent

A sophisticated multi-agent travel agency system built with JADE (Java Agent Development Framework) that simulates an intelligent travel booking platform. The system uses autonomous agents to coordinate travel searches, negotiate prices, and manage reservations through a modern graphical user interface.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [System Components](#system-components)
- [How It Works](#how-it-works)

## Overview

**AgenceVoyageMultiAgent** is an intelligent travel agency system that demonstrates multi-agent system (MAS) principles using the JADE framework. The system allows users to search for travel packages (flights + hotels), compare multiple offers from different providers, and make reservations through an intuitive graphical interface.

### Key Concepts

- **Multi-Agent System**: Multiple autonomous agents collaborate to provide travel services
- **Contract Net Protocol**: FIPA-standard negotiation protocol for agent communication
- **Ontology-Based Communication**: Structured data exchange between agents
- **Distributed Architecture**: Each service provider (airline, hotel) operates independently
- **Real-Time Coordination**: Agents coordinate searches and combine results dynamically

## Features

### Core Functionality

- **Intelligent Travel Search**: Search for flights and hotels by destination, dates, and preferences
- **Budget-Aware Filtering**: Automatically filters results based on user budget constraints
- **Multi-Provider Support**: Multiple airlines and hotel chains compete to offer best deals
- **Comparison Tools**: Side-by-side comparison of multiple travel packages
- **Booking Management**: Create and manage reservations with booking references
- **Booking History**: View past reservations and booking status
- **Modern UI**: Beautiful, responsive graphical interface with gradient themes

### Agent Capabilities

- **Coordinator Agent**: Orchestrates searches and negotiates with service providers
- **Flight Agents**: Each airline (Air France, Ryanair, etc.) has its own agent
- **Hotel Agents**: Specialized agents for luxury and budget hotels
- **UI Agent**: Manages user interface and handles user interactions

## Architecture

### Multi-Agent System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         JADE Platform (Main Container)                      │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        Presentation Layer                            │   │
│  │  ┌──────────────────────────────────────────────────────────────┐   │   │
│  │  │ AgentUI (AgentInterface)                                      │   │   │
│  │  │  ├─ SearchFrame (User Input)                                  │   │   │
│  │  │  ├─ ResultsFrame (Display Results)                            │   │   │
│  │  │  ├─ ComparisonFrame (Compare Packages)                         │   │   │
│  │  │  └─ BookingHistoryDialog (View Bookings)                      │   │   │
│  │  └───────────────────────┬──────────────────────────────────────┘   │   │
│  └──────────────────────────┼──────────────────────────────────────────┘   │
│                             │ REQUEST (RechercherVoyage)                    │
│                             │                                                │
│  ┌──────────────────────────▼──────────────────────────────────────────┐   │
│  │                    Coordination Layer                                │   │
│  │  ┌──────────────────────────────────────────────────────────────┐   │   │
│  │  │ AgentCoordinateur                                             │   │   │
│  │  │  ├─ Directory Facilitator (DF) Service Discovery             │   │   │
│  │  │  ├─ Contract Net Protocol (CFP/PROPOSE/ACCEPT/REJECT)        │   │   │
│  │  │  ├─ Proposal Aggregation & Filtering                         │   │   │
│  │  │  └─ Budget Constraint Validation                              │   │   │
│  │  └───────────────────────┬──────────────────────────────────────┘   │   │
│  └──────────────────────────┼──────────────────────────────────────────┘   │
│                             │ CFP (Call For Proposal)                        │
│                             │                                                │
│  ┌──────────────────────────┼──────────────────────────────────────────┐   │
│  │              Service Provider Layer                                   │   │
│  │                             │                                         │   │
│  │        ┌────────────────────┼────────────────────┐                  │   │
│  │        │                    │                    │                  │   │
│  │  ┌─────▼─────┐      ┌──────▼──────┐    ┌──────▼──────┐          │   │
│  │  │ AgentVol   │      │ AgentVol     │    │ AgentHotel  │          │   │
│  │  │ AirFrance  │      │ Ryanair      │    │ Luxury      │          │   │
│  │  │            │      │              │    │             │          │   │
│  │  │ Query DB   │      │ Query DB     │    │ Query DB    │          │   │
│  │  │ Filter by  │      │ Filter by    │    │ Filter by   │          │   │
│  │  │ Airline    │      │ Airline      │    │ Category    │          │   │
│  │  │            │      │              │    │             │          │   │
│  │  │ PROPOSE    │      │ PROPOSE      │    │ PROPOSE     │          │   │
│  │  │ (VolInfo)  │      │ (VolInfo)    │    │ (HotelInfo) │          │   │
│  │  └─────┬─────┘      └──────┬──────┘    └──────┬──────┘          │   │
│  │        │                    │                    │                  │   │
│  │        └────────────────────┼────────────────────┘                  │   │
│  │                             │                                        │   │
│  │                    ┌─────────▼─────────┐                             │   │
│  │                    │ AgentHotel       │                             │   │
│  │                    │ Budget           │                             │   │
│  │                    │                  │                             │   │
│  │                    │ Query DB         │                             │   │
│  │                    │ Filter by        │                             │   │
│  │                    │ Category         │                             │   │
│  │                    │                  │                             │   │
│  │                    │ PROPOSE          │                             │   │
│  │                    │ (HotelInfo)      │                             │   │
│  │                    └─────────┬─────────┘                             │   │
│  └──────────────────────────────┼──────────────────────────────────────┘   │
│                                 │                                            │
│  ┌──────────────────────────────┼──────────────────────────────────────┐   │
│  │                    Communication Layer                               │   │
│  │  ┌──────────────────────────────────────────────────────────────┐   │   │
│  │  │ VoyageOntology (SLCodec)                                      │   │   │
│  │  │  ├─ Concepts: Voyage, Vol, Hotel, Proposition                │   │   │
│  │  │  ├─ Actions: RechercherVoyage                                │   │   │
│  │  │  └─ Predicates: VoyageInfo, VolInfo, HotelInfo,              │   │   │
│  │  │                PropositionInfo                                 │   │   │
│  │  └──────────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                 │                                            │
│  ┌──────────────────────────────┼──────────────────────────────────────┐   │
│  │                    Data Access Layer                                 │   │
│  │  ┌──────────────────────────────────────────────────────────────┐   │   │
│  │  │ DatabaseManager (Singleton)                                    │   │   │
│  │  │  └─ Connection Pooling                                        │   │   │
│  │  │                                                                │   │   │
│  │  │ ReservationManager                                            │   │   │
│  │  │  └─ Booking Operations                                        │   │   │
│  │  └──────────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────┼──────────────────────────────────────┘   │
│                                 │                                            │
│  ┌──────────────────────────────▼──────────────────────────────────────┐   │
│  │                    Data Persistence Layer                            │   │
│  │  ┌──────────────────────────────────────────────────────────────┐   │   │
│  │  │ MySQL Database (travel_agency)                                  │   │   │
│  │  │  ├─ flights (flight_id, airline, destination, price, seats)      │   │   │
│  │  │  ├─ hotels (hotel_id, name, city, stars, price, rooms)        │   │   │
│  │  │  ├─ reservations (booking_reference, flight_id, hotel_id)      │   │   │
│  │  │  └─ users (user_id, email, full_name)                          │   │   │
│  │  └──────────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘

Message Flow:
  User → AgentUI → AgentCoordinateur → [AgentVol, AgentHotel] → Database
         ← INFORM ← ← PROPOSE ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ←
```

### Design Patterns

- **Multi-Agent System (MAS)**: Distributed autonomous agents
- **Contract Net Protocol**: FIPA standard for agent negotiation
- **Ontology-Based Communication**: Type-safe structured messaging
- **Singleton Pattern**: Database connections, ontology instances
- **Observer Pattern**: UI updates based on agent messages

## Technologies

- **Java**: JDK 8 or higher (tested with Java 21)
- **JADE Framework**: Multi-agent system platform
- **MySQL**: Relational database for flights, hotels, and reservations
- **Swing**: GUI framework for user interface
- **JCalendar**: Date picker component for UI

### Dependencies

- `jade.jar` - JADE framework
- `mysql-connector-j-9.5.0.jar` - MySQL JDBC driver
- `jcalendar-1.4.jar` - Calendar UI component

## Prerequisites

Before running the application, ensure you have:

1. **Java Development Kit (JDK)**
   - Version 8 or higher (Java 21 recommended)
   - Verify installation: `java -version`

2. **MySQL Database Server**
   - Version 5.7 or higher (MariaDB 10.4+ also works)
   - Database server running and accessible

3. **Required JAR Files**
   - All JAR files must be in the `lib/` directory:
     - `jade.jar`
     - `mysql-connector-j-9.5.0.jar`
     - `jcalendar-1.4.jar`

4. **PowerShell** (for Windows) or **Bash** (for Linux/Mac)
   - For running the build scripts

## Installation

### 1. Clone or Download the Repository

```bash
git clone <repository-url>
cd AgenceVoyageMultiAgent
```

### 2. Verify JAR Files

Ensure the `lib/` directory contains:
```
lib/
├── jade.jar
├── mysql-connector-j-9.5.0.jar
└── jcalendar-1.4.jar
```

### 3. Database Configuration

Edit `src/com/agencevoyage/utils/DatabaseManager.java` to configure your database connection:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/travel_agency";
private static final String DB_USER = "your_username";
private static final String DB_PASSWORD = "your_password";
```

## Database Setup

### 1. Create the Database

```sql
CREATE DATABASE travel_agency;
```

### 2. Import Schema and Data

Run the provided SQL script:

```bash
mysql -u your_username -p travel_agency < database.sql
```

Or import via MySQL client:
```sql
USE travel_agency;
SOURCE database.sql;
```

### 3. Verify Database

The database should contain:
- **flights**: Flight information from multiple airlines
- **hotels**: Hotel information in various cities
- **reservations**: Booking records
- **users**: User accounts

### Database Schema

- **flights**: `flight_id`, `flight_code`, `airline`, `origin`, `destination`, `departure_date`, `base_price`, `available_seats`, etc.
- **hotels**: `hotel_id`, `hotel_code`, `name`, `city`, `stars`, `price_per_night`, `available_rooms`, etc.
- **reservations**: `reservation_id`, `booking_reference`, `user_name`, `flight_id`, `hotel_id`, `total_price`, `status`, etc.
- **users**: `user_id`, `email`, `full_name`, `phone`, etc.

## Running the Application

### Option 1: PowerShell Script (Recommended for Windows)

```powershell
.\run.ps1
```

If you encounter execution policy errors:
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run.ps1
```

### Option 2: Batch File (Windows)

```cmd
run.bat
```

### Option 3: Manual Compilation and Run

**Compile:**
```powershell
javac -encoding UTF-8 -d out -cp "lib\jade.jar;lib\mysql-connector-j-9.5.0.jar;lib\jcalendar-1.4.jar" -sourcepath src src\com\agencevoyage\**\*.java
```

**Run:**
```powershell
java -cp "out;lib\jade.jar;lib\mysql-connector-j-9.5.0.jar;lib\jcalendar-1.4.jar" com.agencevoyage.MainLauncher
```

### Expected Behavior

When you run the application:

1. **JADE Platform** starts with GUI showing all agents
2. **Agents are created**:
   - `Coordinateur` - Coordinator agent
   - `AgentVolAirFrance` - Air France flight agent
   - `AgentVolRyanair` - Ryanair flight agent
   - `AgentHotelLuxury` - Luxury hotel agent
   - `AgentHotelBudget` - Budget hotel agent
   - `AgentInterface` - User interface agent

3. **Search Window** appears for travel search
4. **JADE GUI** shows agent status and communication

## Project Structure

```
AgenceVoyageMultiAgent/
├── src/
│   └── com/
│       └── agencevoyage/
│           ├── agents/              # Agent implementations
│           │   ├── AgentCoordinateur.java
│           │   ├── AgentHotel.java
│           │   ├── AgentUI.java
│           │   └── AgentVol.java
│           ├── ontology/            # Communication ontology
│           │   ├── actions/
│           │   │   └── RechercherVoyage.java
│           │   ├── concepts/        # Data structures
│           │   │   ├── Hotel.java
│           │   │   ├── Proposition.java
│           │   │   ├── Vol.java
│           │   │   └── Voyage.java
│           │   ├── predicates/      # Message predicates
│           │   │   ├── HotelInfo.java
│           │   │   ├── PropositionInfo.java
│           │   │   ├── VolInfo.java
│           │   │   └── VoyageInfo.java
│           │   └── VoyageOntology.java
│           ├── ui/                  # User interface components
│           │   ├── SearchFrame.java
│           │   ├── ResultsFrame.java
│           │   ├── ComparisonFrame.java
│           │   ├── BookingHistoryDialog.java
│           │   ├── Theme.java
│           │   ├── UIComponents.java
│           │   └── WindowPositionManager.java
│           ├── utils/               # Utility classes
│           │   ├── DatabaseManager.java
│           │   └── ReservationManager.java
│           ├── MainLauncher.java    # Application entry point
│           └── Test*.java           # Test classes
├── lib/                             # JAR dependencies
├── out/                             # Compiled classes (auto-generated)
├── diagrams/                        # Project diagrams
├── database.sql                     # Database schema and data
├── run.ps1                          # PowerShell build script
├── run.bat                          # Batch build script
└── README.md                        # This file
```

## System Components

### Agents

#### AgentCoordinateur (Coordinator)
- **Role**: Orchestrates travel searches and coordinates with service providers
- **Responsibilities**:
  - Receives search requests from UI agent
  - Discovers service providers using Directory Facilitator (DF)
  - Broadcasts Call For Proposal (CFP) messages
  - Collects and combines proposals from multiple agents
  - Filters results by budget constraints
  - Sends final results to UI agent

#### AgentVol (Flight Agents)
- **Role**: Represents airline companies (Air France, Ryanair, etc.)
- **Responsibilities**:
  - Responds to CFP messages with flight offers
  - Queries database for available flights
  - Filters flights by destination and date
  - Sends PROPOSE messages with flight details
  - Handles booking confirmations

#### AgentHotel (Hotel Agents)
- **Role**: Represents hotel providers (Luxury, Budget categories)
- **Responsibilities**:
  - Responds to CFP messages with hotel offers
  - Queries database for available hotels
  - Filters hotels by city, stars, and availability
  - Sends PROPOSE messages with hotel details
  - Manages room availability

#### AgentUI (User Interface Agent)
- **Role**: Manages user interface and user interactions
- **Responsibilities**:
  - Displays search interface (SearchFrame)
  - Sends search requests to coordinator
  - Receives and displays search results
  - Handles booking creation
  - Manages booking history display

### Ontology

The system uses a custom ontology (`VoyageOntology`) for structured agent communication:

- **Concepts**: `Voyage`, `Vol`, `Hotel`, `Proposition`
- **Actions**: `RechercherVoyage`
- **Predicates**: `VoyageInfo`, `VolInfo`, `HotelInfo`, `PropositionInfo`

### Database Layer

- **DatabaseManager**: Handles MySQL connections and connection pooling
- **ReservationManager**: Manages booking operations and reservation persistence

## How It Works

### Complete Search Flow

1. **User Initiates Search**
   - User fills out search form (destination, dates, passengers, rooms, budget)
   - Clicks "Search" button

2. **UI Agent Processing**
   - `AgentUI` creates a `Voyage` object from form data
   - Finds `AgentCoordinateur` using Directory Facilitator (DF)
   - Sends REQUEST message with `RechercherVoyage` action

3. **Coordinator Processing**
   - `AgentCoordinateur` receives REQUEST
   - Sends AGREE acknowledgment
   - Searches DF for service providers:
     - "vente-vol" agents (flight providers)
     - "vente-hotel" agents (hotel providers)
   - Creates and broadcasts CFP (Call For Proposal) messages

4. **Service Provider Responses**
   - **Flight Agents** query database for matching flights
   - **Hotel Agents** query database for matching hotels
   - Each agent sends PROPOSE messages with offers

5. **Coordinator Aggregation**
   - Collects all proposals
   - Combines flights and hotels into travel packages
   - Filters by budget constraints
   - Removes duplicates
   - Sends ACCEPT_PROPOSAL to selected providers

6. **Results Delivery**
   - Coordinator sends INFORM messages to UI agent
   - UI agent displays results in `ResultsFrame`
   - User can compare options and make bookings

### Message Protocol

The system uses FIPA ACL (Agent Communication Language) messages:

- **REQUEST**: UI → Coordinator (search request)
- **AGREE**: Coordinator → UI (acknowledgment)
- **CFP**: Coordinator → Providers (request for offers)
- **PROPOSE**: Providers → Coordinator (offer)
- **ACCEPT_PROPOSAL**: Coordinator → Providers (accept offer)
- **REJECT_PROPOSAL**: Coordinator → Providers (reject offer)
- **INFORM**: Coordinator → UI (send results)
- **FAILURE**: Coordinator → UI (error/no results)

## License

This project is developed for educational purposes as part of a multi-agent systems course.

## Authors

- Project developed as part of academic coursework
- Multi-agent travel agency system implementation

## Acknowledgments

- JADE Framework team for the excellent multi-agent platform
- FIPA for standardized agent communication protocols
- MySQL community for robust database solutions

---

**Note**: This is an educational project demonstrating multi-agent system principles. For production use, additional security, error handling, and scalability considerations would be required.
