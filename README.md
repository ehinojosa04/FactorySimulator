# Factory Simulation System

## Project Information

**Institution:** Universidad Panamericana  
**Program:** Ingeniería en Sistemas y Gráficas Computacionales  
**Course:** Fundamentos de Programación en Paralelo  
**Professor:** Dr. Juan Carlos López Pimentel  

**Team Members:**
- Diego Amín Hernández Pallares
- Emiliano Hinojosa Guzmán
- José Salcedo Uribe

**Submission Date:** December 4, 2025

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Agent Types](#agent-types)
- [Facilities](#facilities)
- [UI Components](#ui-components)
- [Technical Details](#technical-details)

---

## Overview

This project is a comprehensive multi-threaded factory simulation system built in Java. It demonstrates advanced concepts in concurrent programming, including thread management, synchronization, socket communication, and real-time visualization. The simulation models a complete production facility with workers, managers, inventory control, delivery systems, and shared resources with capacity constraints.

---

## Features

### Core Simulation
- **Multi-Agent System**: Workers, managers, inventory agents, and delivery agents operating concurrently
- **Production Pipeline**: Order management, material requisition, production, and inventory updates
- **Resource Management**: Semaphore-based workstation allocation with configurable capacity
- **Supply Chain**: Automated material ordering and delivery system with truck capacity management

### Facilities & Breaks
- **Breakroom Server** (Port 5001): Workers can take extended breaks (10 seconds)
- **Bathroom Server** (Port 5002): Workers can take short breaks (5 seconds)
- **Queue Management**: FIFO-based facility access with capacity limits
- **Network Protocol**: Custom TCP protocol for facility-agent communication

### Real-Time Monitoring
- **Agent States Window**: Live table view of all agent states, locations, and activities
- **Factory Visualization**: 2D animated representation with agent movement between zones
- **Inventory Monitor**: Real-time warehouse inventory levels
- **Thread States Dashboard**: Thread state statistics grouped by agent type
- **Zones Overview**: Agent distribution across locations

---

## Architecture

### Package Structure

```
src/
├── core/
│   ├── agents/          # Base agent classes and enums
│   ├── ui/              # Visualization windows
│   └── Zones/           # Buffer zone management
├── factory/
│   ├── agents/          # Specialized agent implementations
│   ├── production/      # Production orders and workstations
│   └── warehouse/       # Inventory management
└── Facility/            # Facility servers and connections
```

### Key Components

#### Core Layer
- **BaseAgent**: Abstract thread-based agent with state machine logic
- **AgentState**: Enum defining agent states (IDLE, WORKING, WAITING, MOVING, ON_BREAK, etc.)
- **AgentLocation**: Enum for factory zones (FACTORY, WAREHOUSE, BATHROOM, BREAKROOM, LOADING_DECK, SUPPLIER)
- **ZonesAPI**: Centralized access to shared resources

#### Factory Layer
- **Factory**: Main factory coordinator managing all agents and resources
- **FactoryServer**: Initializes simulation and UI components
- **FactoryLauncher**: GUI configuration interface

#### Agent Implementations
- **WorkerAgent**: Handles production orders, material gathering, and manufacturing
- **ManagerAgent**: Plans production and manages worker lifecycle
- **InventoryAgent**: Coordinates material requisitions and delivery assignments
- **DeliveryAgent**: Transports materials from suppliers to warehouse

#### Facility Layer
- **FacilityServer**: Abstract server for break facilities
- **BathroomServer** / **BreakRoomServer**: Concrete implementations with TCP servers
- **FacilityConnection**: Client-side connection handler with event protocol
- **Facility**: Semaphore-based capacity management with lifecycle hooks

---

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code with Java extensions)

### Running the Simulation

1. **Start Facility Servers** (in separate terminals/processes):
   ```bash
   java Facility.BreakRoomServer
   java Facility.BathroomServer
   ```

2. **Launch Factory Simulation**:
   ```bash
   java factory.FactoryLauncher
   ```

3. **Configure Parameters** in the launcher GUI and click "Start Simulation"

### Folder Structure
- `src/`: Source code
- `lib/`: Dependencies (if any)
- `bin/`: Compiled output (auto-generated)

---

## Configuration

The **FactoryLauncher** provides a GUI to configure:

| Parameter | Description | Default |
|-----------|-------------|---------|
| Workers | Number of factory workers | 10 |
| Delivery | Number of delivery agents | 3 |
| Truck Max Capacity | Items per delivery truck | 20 |
| Order Batch Size | Orders created per batch | 10 |
| Types of Products | Different product types in inventory | 5 |
| Workstation Agent Capacity | Max concurrent workers at workstations | 2 |
| Time for Transportation | Delivery truck travel time (ms) | 10000 |
| Time to Produce Item | Manufacturing time per item (ms) | 500 |
| Time to Request Materials | Material requisition delay (ms) | 500 |

---

## Agent Types

### Worker Agent
**Responsibilities:**
- Request production orders from queue
- Requisition raw materials from inventory agent
- Travel to warehouse to collect materials
- Compete for workstation access (semaphore-controlled)
- Manufacture products
- Update warehouse inventory with finished goods
- Take breaks at bathroom/breakroom facilities

**State Machine:**
- IDLE → Request order → MOVING (to warehouse)
- WAITING → Collect materials → MOVING (to factory)
- WORKING → Manufacturing → Complete order → IDLE
- Any state → ON_BREAK (if conditions met) → Return to FACTORY

**Break Logic:**
- After 3+ completed orders, probabilistic break chance increases
- Break location randomly selected (bathroom or breakroom)
- Communicates with facility servers via TCP sockets
- Releases workstation before taking break

### Manager Agent
**Responsibilities:**
- Generate production orders in batches
- Hire and initialize all agent types
- Start agent threads
- Monitor production (future enhancement)

**Behavior:**
- Runs continuously as daemon thread
- Generates new order batches when queue is empty
- One manager per factory instance

### Inventory Agent
**Responsibilities:**
- Receive material requisitions from workers
- Assign delivery orders to available delivery agents
- Track pending material orders
- Manage delivery agent queue

**States:**
- WORKING: Assigning orders to delivery agents
- WAITING: Materials needed but no available drivers
- IDLE: Monitoring inventory levels

**Logic:**
- Batches orders to optimize truck capacity
- Only assigns to delivery agents at LOADING_DECK with no active orders

### Delivery Agent
**Responsibilities:**
- Transport materials from SUPPLIER to WAREHOUSE
- Load cargo up to truck capacity
- Unload cargo at warehouse
- Handle partial orders requiring multiple trips

**State Machine:**
- WAITING (at LOADING_DECK) → Receive order → MOVING (to SUPPLIER)
- WORKING (at SUPPLIER) → Load truck → MOVING (to WAREHOUSE)
- WORKING (at WAREHOUSE) → Unload → Check order completion
  - If incomplete: Return to SUPPLIER
  - If complete: Return to LOADING_DECK

**Features:**
- Incremental loading/unloading animations
- Capacity tracking (cargo/maxCapacity)
- Multi-trip order fulfillment

---

## Facilities

### Communication Protocol

**Client → Server:**
- `HELLO <agentId>`: Initial handshake
- `REQUEST_BREAKROOM`: Request breakroom access
- `REQUEST_BATHROOM`: Request bathroom access
- `QUIT`: Close connection

**Server → Client:**
- `STATE <agentId> <state>`: Update agent state
- `LOCATION <agentId> <location>`: Update agent location
- `EVENT <agentId> <eventType>`: Notify agent of events
  - `HELLO_OK`: Connection established
  - `BREAK_COMPLETE`: Break finished, return to factory
  - `BYE`: Connection closing

### Facility Lifecycle

1. **Agent requests access** via facility connection
2. **Server queues request** (FIFO via fair semaphore)
3. **Agent enters** when capacity available
   - State set to ON_BREAK
   - Movement animation to facility
4. **Agent uses facility** (timed delay)
5. **Agent exits**
   - State set to IDLE
   - Location set to FACTORY
   - BREAK_COMPLETE event sent
6. **Connection closed** by agent

### Capacity Limits
- **Breakroom**: 10 concurrent agents
- **Bathroom**: 5 concurrent agents
- **Workstations**: Configurable (default 2)

---

## UI Components

### Agent States Window
**Features:**
- Live table with columns: Agent ID, Type, State, Location, Activity Description
- Color-coded state indicators:
  - 🟢 WORKING (Green)
  - 🔴 WAITING (Red)
  - 🔵 MOVING (Blue)
  - ⚫ IDLE (Gray)
  - 🟠 ON_BREAK (Orange)
- Detailed activity descriptions per agent
- 100ms update rate for smooth text updates

### Factory Visualization Window
**Features:**
- 2D animated zone representation
- Agent movement with smooth interpolation (2-second transitions)
- Color-coded agents by type:
  - Workers: Steel Blue circles
  - Managers: Dark Red triangles
  - Inventory: Goldenrod rounded squares
  - Delivery: Sea Green squares
- State indicator dots on agents
- Queue bubbles showing waiting agent counts
- Manager and Inventory agents pinned to central positions
- Random position offsets within zones for readability

**Zones:**
- BATHROOM (Purple): Top-left
- BREAKROOM (Light Green): Top-right
- WAREHOUSE (Peach): Middle-left
- FACTORY (Light Blue): Middle-right
- LOADING_DECK (Light Yellow): Bottom-left
- SUPPLIER (Light Pink): Bottom-right

### Inventory Window
**Features:**
- Real-time inventory slot values
- Grid layout for all product types
- 1-second update rate

### Thread States Window
**Features:**
- Thread state summary per agent type
- Columns: RUNNABLE, WAITING, TIMED_WAITING, BLOCKED, TERMINATED
- Useful for debugging concurrency issues

### Zones Window
**Features:**
- Overview dashboard
- Agent count by type
- Agent count by location
- Clean text-based summary

---

## Technical Details

### Concurrency Mechanisms

#### Semaphores (Fair)
- **Workstations**: Controls concurrent access to manufacturing stations
- **Facilities**: Manages bathroom/breakroom capacity with FIFO queuing

#### ReentrantLocks
- **Warehouse**: Protects inventory array from race conditions
- **InventoryAgent**: Synchronizes material order tracking

#### Thread Safety
- All agent state transitions are atomic
- Socket communication isolated per agent connection
- UI updates dispatched via SwingUtilities.invokeLater()

### Network Architecture
- **Protocol**: Custom text-based TCP protocol
- **Connection Management**: One persistent connection per agent per facility
- **Server Model**: Multi-threaded server with ClientHandler per connection
- **Error Handling**: Graceful disconnection and reconnection logic

### State Machine Design
Each agent implements a two-phase state machine:
1. **processNextState()**: Determines state transitions based on conditions
2. **performLocationBehavior()**: Executes behavior for current state/location

This separation allows for clean, maintainable state logic.

### Animation System
- **Movement**: Linear interpolation between zone centers over configurable duration
- **Randomization**: Agents offset randomly within zones to prevent overlap
- **Frame Rate**: 16ms update cycle (~60 FPS) for smooth animation

---

## Known Limitations

1. **Material Types**: Currently simplified to single material type (index 0)
2. **Order Complexity**: Basic order structure without BOM (Bill of Materials)
3. **Server Fault Tolerance**: Facility servers must be running before worker breaks
4. **UI Scaling**: Fixed window sizes, not responsive to screen resolution

---

## Future Enhancements

- [ ] Multi-material product recipes
- [ ] Dynamic production planning algorithms
- [ ] Performance metrics dashboard
- [ ] Save/load simulation state
- [ ] Configurable break frequencies per agent
- [ ] Machine breakdown and maintenance cycles
- [ ] Quality control stations
- [ ] Shift scheduling system

---

## License

This project is developed for educational purposes as part of the Parallel Programming course at Universidad Panamericana.

---

## Acknowledgments

Special thanks to Dr. Juan Carlos López Pimentel for guidance on concurrent programming concepts and project requirements.
