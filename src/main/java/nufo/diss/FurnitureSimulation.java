package nufo.diss;

import nufo.diss.generators.*;

import java.util.*;
import java.util.stream.Collectors;

public class FurnitureSimulation extends EventSimulation{
    private final Map<Order.State, Queue<Order>> orderQueues;
    private final Map<Carpenter.Group, Integer> carpentersGroupSizes;
    private final Map<Carpenter.Group, List<Carpenter>> carpenterGroups;
    private final List<Workplace> workplaces;
    private Random orderTypeProbabilityGenerator;
    private ExponentialGenerator orderArrivalGenerator;
    private TriangularGenerator timeToMoveBetweenWorkplaceAndWarehouseGenerator;
    private TriangularGenerator timeToPrepareMaterialInWarehouseGenerator;
    private TriangularGenerator timeToMoveBetweenWorkplacesGenerator;

    private Map<Order.Type, Map<Order.State, Generator>> actionTimeGenerators;

    private final FurnitureSimulationReplicationStatistics replicationStatistics;
    private final FurnitureSimulationExperimentStatistics experimentStatistics;

    private int numberOfArrivedOrders;
    private int numberOfDoneOrders;

    protected FurnitureSimulation(
            int numberOfReplications,
            TimeMode timeMode,
            double maxTime,
            int carpenterGroupASize,
            int carpenterGroupBSize,
            int carpenterGroupCSize
    ) {
        super(numberOfReplications, timeMode, maxTime);

        carpentersGroupSizes = new Hashtable<>();
        carpentersGroupSizes.put(Carpenter.Group.A, carpenterGroupASize);
        carpentersGroupSizes.put(Carpenter.Group.B, carpenterGroupBSize);
        carpentersGroupSizes.put(Carpenter.Group.C, carpenterGroupCSize);

        orderQueues = new Hashtable<>();
        for (Order.State state : Order.State.queueValues()) {
            orderQueues.put(state, new LinkedList<>());
        }

        workplaces = new LinkedList<>();

        carpenterGroups = new Hashtable<>();

        replicationStatistics = new FurnitureSimulationReplicationStatistics();
        experimentStatistics = new FurnitureSimulationExperimentStatistics();
    }

    public static class FurnitureSimulationReplicationStatistics {
        private final Statistics orderWorkingTime = new Statistics();
        private final Statistics notYetStartedWorkOrders = new Statistics();
        private final Map<Carpenter.Group, Statistics> groupWorkloads = new Hashtable<>();
        private final Map<Carpenter, Statistics> carpenterWorkloads = new Hashtable<>();
    }

    public static class FurnitureSimulationExperimentStatistics {
        private final Statistics orderWorkingTime = new Statistics();

        public void reset() {
            orderWorkingTime.reset();
        }
    }

    public static class Carpenter {
        private State state = State.FREE;
        private Position position = Position.WAREHOUSE;
        private Workplace workplace;
        private final Group group;
        public static int carpenterId = 0;
        private final int id = ++carpenterId;
        private double workTime = 0.0;
        private double lastWorkStartTime = 0.0;

        Carpenter(Group group) {
            this.group = group;
        }

        public int getId() {
            return id;
        }

        public State getState() {
            return state;
        }

        public void setState(State state , double time) {
            if (this.state == state) {
                throw new IllegalStateException("State is already set: " + state);
            }

            if (state == State.WORKING) {
                lastWorkStartTime = time;
            } else if (state == State.FREE) {
                workTime += time - lastWorkStartTime;
            }

            this.state = state;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }

        public void setWorkplace(Workplace workplace) {
            this.workplace = workplace;
        }

        public Workplace getWorkplace() {
            return workplace;
        }

        public Group getGroup() {
            return group;
        }

        public void reset() {
            state = State.FREE;
            position = Position.WAREHOUSE;
            workplace = null;
            workTime = 0.0;
            lastWorkStartTime = 0.0;
        }

        public static void resetCarpenterId() {
            carpenterId = 0;
        }

        public enum State {
            FREE,
            WORKING
        }

        public enum Position {
            WAREHOUSE,
            WORKPLACE
        }

        public enum Group {
            A, B, C
        }

        @Override
        public String toString() {
            return "ID: " + id +
                    ", state: " + state +
                    (workplace != null ? ", workplaceID: " + workplace.getId() : "") +
                    ", position: " + position +
                    ", group: " + group;
        }

        public Data toData() {
            return new Data(getId(), getGroup(), getState(), getPosition(), getWorkplace() != null ? getWorkplace().getId() : null);
        }

        public record Data(
                int id,
                Group group,
                State state,
                Position position,
                Integer workplaceId
        ) { }
    }

    public static class Order {
        private final Type type;
        private State state;
        private Workplace workplace;
        private final double arrivalTime;

        Order(Type type, double arrivalTime) {
            this.type = type;
            this.state = State.NEW;
            this.arrivalTime = arrivalTime;
        }

        public Type getType() {
            return type;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public void setWorkplace(Workplace workplace) {
            if (this.workplace != null) {
                throw new IllegalStateException("Order already in the workplace.");
            }
            this.workplace = workplace;
        }

        public Workplace getWorkplace() {
            return workplace;
        }

        public double getArrivalTime() {
            return arrivalTime;
        }

        public enum Type {
            TABLE,
            CHAIR,
            WARDROBE
        }

        public enum State {
            NEW,
            SAWING,
            SAWED,
            SOAKING,
            SOAKED,
            ASSEMBLING,
            ASSEMBLED,
            FITTINGS_INSTALLATION,
            DONE;

            public static State[] queueValues() {
                return new State[] {NEW, SAWED, SOAKED, ASSEMBLED};
            }
        }
    }

    public static class Workplace {
        public static int workplaceId = 0;
        private final int id = ++workplaceId;
        private Order order;
        private Carpenter carpenter;

        public int getId() {
            return id;
        }

        public boolean isFree() {
            return order == null;
        }

        public void assignOrder(Order order) {
            if (this.order != null) {
                throw new IllegalStateException("Only one order can be in workplace.");
            }
            order.setWorkplace(this);
            this.order = order;
        }

        public void unassignOrder() {
            if (order == null) {
                throw new IllegalStateException("The is no order in workplace.");
            }
            order = null;
        }

        public Order getOrder() {
            return order;
        }

        public Carpenter getCarpenter() {
            return carpenter;
        }

        public void assignCarpenter(Carpenter carpenter) {
            if (this.carpenter != null) {
                throw new IllegalStateException("Only one carpenter can be in workplace.");
            }
            this.carpenter = carpenter;
        }

        public void unassignCarpenter() {
            if (carpenter == null) {
                throw new IllegalStateException("No carpenter is in workplace.");
            }
            this.carpenter = null;
        }

        public static void resetWorkplaceId() {
            workplaceId = 0;
        }

        @Override
        public String toString() {
            return "ID: " + id +
                    ", order: " + (order != null ? "Order( type: "+order.getType()+", state: "+order.getState()+" )" : "NONE") +
                    (carpenter != null ? ", carpenterID: " + carpenter.getId() : "");
        }
    }

    public record EventData(
            double currentTime,
            int numberOfArrivedOrders,
            int numberOfDoneOrders,
            Map<Order.State, Integer> queueSizes,
            String[] workplaces,
            String[] carpenters,
            Statistics orderWorkingTime
    ) { }

    public record ReplicationData(
         int numberOfDoneReplications,
         Statistics.Data orderWorkingTime,
         Statistics.Data notYetStartedWorkOrders,
         Map<Carpenter.Group, Statistics.Data> carpenterGroupWorkloads,
         Map<Carpenter.Data, Statistics.Data> carpenterWorkloads
    ) { }

    @Override
    protected void notifyStateChange(StateChangeType stateChangeType) {
        if (consumer != null) {
            consumer.accept(switch (stateChangeType) {
                case EXPERIMENT -> new ConsumerData(stateChangeType, new ReplicationData(
                        doneReplications,
                        replicationStatistics.orderWorkingTime.toData(),
                        replicationStatistics.notYetStartedWorkOrders.toData(),
                        replicationStatistics.groupWorkloads.entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toData())),
                        replicationStatistics.carpenterWorkloads.entrySet()
                                .stream()
                                .collect(Collectors.toMap(e -> e.getKey().toData(), e -> e.getValue().toData()))
                ));
                case EVENT -> new ConsumerData(stateChangeType, new EventData(
                        currentTime,
                        numberOfArrivedOrders,
                        numberOfDoneOrders,
                        orderQueues.entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size())),
                        this.workplaces.stream().map(Workplace::toString).toArray(String[]::new),
                        carpenterGroups.values().stream()
                                .flatMap(List::stream)
                                .sorted(Comparator.comparingInt(Carpenter::getId))
                                .map(Carpenter::toString)
                                .toArray(String[]::new),
                        experimentStatistics.orderWorkingTime
                ));
                case STATE -> new ConsumerData(stateChangeType, state);
            });
        }
    }

    @Override
    protected void beforeSimulation() {
        orderArrivalGenerator = new ExponentialGenerator(2.0 / 3600.0);
        orderTypeProbabilityGenerator = new Random(SeedGenerator.getInstance().nextInt());

        timeToMoveBetweenWorkplaceAndWarehouseGenerator = new TriangularGenerator(60, 480, 120);
        timeToPrepareMaterialInWarehouseGenerator = new TriangularGenerator(300, 900, 500);
        timeToMoveBetweenWorkplacesGenerator = new TriangularGenerator(120, 500, 150);

        actionTimeGenerators = new Hashtable<>();
        for (Order.Type orderType : Order.Type.values()) {
            actionTimeGenerators.put(orderType, new Hashtable<>());
        }

        actionTimeGenerators.get(Order.Type.TABLE).put(Order.State.SAWING, new EmpiricalGenerator(
                Arrays.asList(10.0 * 60, 25.0 * 60),
                Arrays.asList(25.0 * 60, 50.0 * 60),
                Arrays.asList(0.6, 0.4),
                Generator.Mode.CONTINUOUS
        ));
        actionTimeGenerators.get(Order.Type.TABLE).put(Order.State.SOAKING, new UniformGenerator(200 * 60, 610 * 60, Generator.Mode.CONTINUOUS));
        actionTimeGenerators.get(Order.Type.TABLE).put(Order.State.ASSEMBLING, new UniformGenerator(30 * 60, 60 * 60, Generator.Mode.CONTINUOUS));

        actionTimeGenerators.get(Order.Type.CHAIR).put(Order.State.SAWING, new UniformGenerator(12 * 60, 16 * 60, Generator.Mode.CONTINUOUS));
        actionTimeGenerators.get(Order.Type.CHAIR).put(Order.State.SOAKING, new UniformGenerator(210 * 60, 540 * 60, Generator.Mode.CONTINUOUS));
        actionTimeGenerators.get(Order.Type.CHAIR).put(Order.State.ASSEMBLING, new UniformGenerator(14 * 60, 24 * 60, Generator.Mode.CONTINUOUS));

        actionTimeGenerators.get(Order.Type.WARDROBE).put(Order.State.SAWING, new UniformGenerator(15 * 60, 80 * 60, Generator.Mode.CONTINUOUS));
        actionTimeGenerators.get(Order.Type.WARDROBE).put(Order.State.SOAKING, new UniformGenerator(600 * 60, 700 * 60, Generator.Mode.CONTINUOUS));
        actionTimeGenerators.get(Order.Type.WARDROBE).put(Order.State.ASSEMBLING, new UniformGenerator(35 * 60, 75 * 60, Generator.Mode.CONTINUOUS));
        actionTimeGenerators.get(Order.Type.WARDROBE).put(Order.State.FITTINGS_INSTALLATION, new UniformGenerator(15 * 60, 25 * 60, Generator.Mode.CONTINUOUS));

        Carpenter.resetCarpenterId();
        for (Carpenter.Group group : Carpenter.Group.values()) {
            List<Carpenter> carpenters = new LinkedList<>();
            for (int i = 0; i < carpentersGroupSizes.get(group); i++) {
                Carpenter carpenter = new Carpenter(group);
                carpenters.add(carpenter);
                replicationStatistics.carpenterWorkloads.put(carpenter, new Statistics());
            }
            carpenterGroups.put(group, carpenters);
            replicationStatistics.groupWorkloads.put(group, new Statistics());
        }
    }

    @Override
    protected void afterSimulation() {

    }

    @Override
    protected void beforeExperiment() {
        currentTime = 0.0;
        numberOfArrivedOrders = 0;
        numberOfDoneOrders = 0;
        resetEventCalendar();

        for(Order.State state : Order.State.queueValues()) {
            orderQueues.get(state).clear();
        }

        workplaces.clear();
        Workplace.resetWorkplaceId();

        for (Carpenter.Group group : Carpenter.Group.values()) {
            for (Carpenter carpenter: carpenterGroups.get(group)) {
                carpenter.reset();
            }
        }

        addEvent(new OrderArrivalEvent(this, getNextOrderArrivalTime()));

        experimentStatistics.reset();
    }

    @Override
    protected void afterExperiment() {
        replicationStatistics.orderWorkingTime.addValue(experimentStatistics.orderWorkingTime.getMean());
        replicationStatistics.notYetStartedWorkOrders.addValue(orderQueues.get(Order.State.NEW).size());

        for (Carpenter.Group group : Carpenter.Group.values()) {
            List<Carpenter> carpenters = carpenterGroups.get(group);
            replicationStatistics.groupWorkloads.get(group).addValue(carpenters.stream().mapToDouble(c -> c.workTime).sum() / (currentTime * carpenters.size()));

            for (Carpenter carpenter : carpenters) {
                replicationStatistics.carpenterWorkloads.get(carpenter).addValue(carpenter.workTime / currentTime);
            }
        }
    }

    public Carpenter getFreeCarpenterFromGroup(Carpenter.Group group) {
        for (Carpenter carpenter : carpenterGroups.get(group)) {
            if (carpenter.getState() == Carpenter.State.FREE) {
                return carpenter;
            }
        }
        return null;
    }

    public void addOrderToQueue(Order order) {
        if (!orderQueues.get(order.getState()).add(order)) {
            throw new RuntimeException("Something went wrong while adding to orders queue.");
        }
    }

    public Order getNextOrderFromQueue(Order.State state) {
        return orderQueues.get(state).poll();
    }

    public double getNextOrderArrivalTime() {
        if (orderArrivalGenerator == null) {
            throw new IllegalStateException("Order arrival generator not set.");
        }
        return orderArrivalGenerator.nextDouble();
    }

    public double getNextTimeToMoveBetweenWorkplaceAndWarehouse() {
        if (timeToMoveBetweenWorkplaceAndWarehouseGenerator == null) {
            throw new IllegalStateException("Time to move between assembly site and warehouse generator not set.");
        }
        return timeToMoveBetweenWorkplaceAndWarehouseGenerator.nextDouble();
    }

    public double getNextTimeToPrepareMaterialInWarehouse() {
        if (timeToPrepareMaterialInWarehouseGenerator == null) {
            throw new IllegalStateException("Time to prepare material in warehouse generator not set.");
        }
        return timeToPrepareMaterialInWarehouseGenerator.nextDouble();
    }

    public double getNextTimeToMoveBetweenWorkplaces() {
        if (timeToMoveBetweenWorkplacesGenerator == null) {
            throw new IllegalStateException("Time to move between assembly sites generator not set.");
        }
        return timeToMoveBetweenWorkplacesGenerator.nextDouble();
    }

    public double getNextActionTime(Order order) {
        return actionTimeGenerators.get(order.getType()).get(order.getState()).nextDouble();
    }

    public void addWorkplace(Workplace workplace) {
        workplaces.add(workplace);
    }

    public Workplace getFreeWorkplace() {
        for (Workplace workplace : workplaces) {
            if (workplace.isFree()) {
                return workplace;
            }
        }

        Workplace workplace = new Workplace();
        addWorkplace(workplace);

        return workplace;
    }

    public Order.Type getNextOrderType() {
        if (orderTypeProbabilityGenerator == null) {
            throw new IllegalStateException("Order type probability generator not set.");
        }

        double orderTypeProbability = orderTypeProbabilityGenerator.nextDouble();

        if (orderTypeProbability < 0.5) {
            return Order.Type.TABLE;
        } else if (orderTypeProbability < 0.65) {
            return Order.Type.CHAIR;
        } else {
            return Order.Type.WARDROBE;
        }
    }

    public void incrementNumberOfArrivedOrders() {
        numberOfArrivedOrders++;
    }

    public void incrementNumberOfDoneOrders() {
        numberOfDoneOrders++;
    }

    public double getCarpenterMoveTime(Carpenter carpenter, Workplace toWorkplace) {
        return switch (carpenter.getPosition()) {
            case WORKPLACE -> carpenter.getWorkplace().getId() != toWorkplace.getId() ? getNextTimeToMoveBetweenWorkplaces() : 0.0;
            case WAREHOUSE -> getNextTimeToMoveBetweenWorkplaceAndWarehouse();
        };
    }

    // ------------------------------------------------ EVENTS ------------------------------------------------
    private abstract static class FurnitureSimulationEvent extends Event {
        protected final Workplace workplace;

        public FurnitureSimulationEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime);
            this.workplace = workplace;
        }

        public FurnitureSimulationEvent(FurnitureSimulation simulation, double executionTime) {
            this(simulation, executionTime, null);
        }
    }

    private static class OrderArrivalEvent extends FurnitureSimulationEvent {
        public OrderArrivalEvent(FurnitureSimulation simulation, double executionTime) {
            super(simulation, executionTime);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            Order order = new Order(simulation.getNextOrderType(), executionTime);
            simulation.incrementNumberOfArrivedOrders();

            Carpenter freeCarpenter = simulation.getFreeCarpenterFromGroup(Carpenter.Group.A);
            if (freeCarpenter != null) {
                Workplace workplace = simulation.getFreeWorkplace();
                workplace.assignOrder(order);
                workplace.assignCarpenter(freeCarpenter);
                simulation.addEvent(new FurnitureSawingStartEvent(simulation, executionTime, workplace));
            } else {
                simulation.addOrderToQueue(order);
            }

            simulation.addEvent(new OrderArrivalEvent(simulation, executionTime + simulation.getNextOrderArrivalTime()));
        }
    }

    private static class FurnitureSawingStartEvent extends FurnitureSimulationEvent {
        public FurnitureSawingStartEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime, workplace);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            Carpenter carpenter = workplace.getCarpenter();

            carpenter.setState(Carpenter.State.WORKING, executionTime);

            Order order = workplace.getOrder();
            order.setState(Order.State.SAWING);

            double endTime = executionTime;

            if (carpenter.getPosition() == Carpenter.Position.WORKPLACE) {
                endTime += simulation.getNextTimeToMoveBetweenWorkplaceAndWarehouse();
            }

            endTime += simulation.getNextTimeToPrepareMaterialInWarehouse() + simulation.getNextTimeToMoveBetweenWorkplaceAndWarehouse();

            endTime += simulation.getNextActionTime(order);
            
            carpenter.setPosition(Carpenter.Position.WORKPLACE);
            carpenter.setWorkplace(workplace);

            simulation.addEvent(new FurnitureSawingEndEvent(simulation, endTime, workplace));
        }
    }

    private static class FurnitureSawingEndEvent extends FurnitureSimulationEvent {
        public FurnitureSawingEndEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime, workplace);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            Order order = workplace.getOrder();
            order.setState(Order.State.SAWED);
            workplace.getCarpenter().setState(Carpenter.State.FREE, executionTime);
            workplace.unassignCarpenter();

            Carpenter freeCarpenter = simulation.getFreeCarpenterFromGroup(Carpenter.Group.C);
            if (freeCarpenter != null) {
                workplace.assignCarpenter(freeCarpenter);
                simulation.addEvent(new FurnitureSoakingStartEvent(simulation, executionTime, workplace));
            } else {
                simulation.addOrderToQueue(order);
            }

            Order nextNewOrder = simulation.getNextOrderFromQueue(Order.State.NEW);
            if (nextNewOrder != null) {
                Workplace freeWorkplace = simulation.getFreeWorkplace();
                freeWorkplace.assignOrder(nextNewOrder);
                freeWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(Carpenter.Group.A));
                simulation.addEvent(new FurnitureSawingStartEvent(simulation, executionTime, freeWorkplace));
            }
        }
    }

    private static class FurnitureSoakingStartEvent extends FurnitureSimulationEvent {
        public FurnitureSoakingStartEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime, workplace);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            Carpenter carpenter = workplace.getCarpenter();
            carpenter.setState(Carpenter.State.WORKING, executionTime);

            Order order = workplace.getOrder();
            order.setState(Order.State.SOAKING);

            double endTime = simulation.getNextActionTime(order);

            endTime += executionTime + simulation.getCarpenterMoveTime(carpenter, workplace);

            carpenter.setPosition(Carpenter.Position.WORKPLACE);
            carpenter.setWorkplace(workplace);
            simulation.addEvent(new FurnitureSoakingEndEvent(simulation, endTime, workplace));
        }
    }

    private static class FurnitureSoakingEndEvent extends FurnitureSimulationEvent {
        public FurnitureSoakingEndEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime, workplace);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            workplace.getCarpenter().setState(Carpenter.State.FREE, executionTime);

            Order order = workplace.getOrder();
            order.setState(Order.State.SOAKED);
            workplace.unassignCarpenter();

            Carpenter freeCarpenter = simulation.getFreeCarpenterFromGroup(Carpenter.Group.B);
            if (freeCarpenter != null) {
                workplace.assignCarpenter(freeCarpenter);
                simulation.addEvent(new FurnitureAssemblingStartEvent(simulation, simulation.getCurrentTime(), workplace));
            } else {
                simulation.addOrderToQueue(order);
            }

            Order nextAssembledOrder = simulation.getNextOrderFromQueue(Order.State.ASSEMBLED);
            if (nextAssembledOrder != null) {
                Workplace nextAssembledOrderWorkplace = nextAssembledOrder.getWorkplace();
                nextAssembledOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(Carpenter.Group.C));
                simulation.addEvent(new FurnitureFittingsInstallationStartEvent(simulation, executionTime, nextAssembledOrderWorkplace));
            } else {
                Order nextSawedOrder = simulation.getNextOrderFromQueue(Order.State.SAWED);
                if (nextSawedOrder != null) {
                    Workplace nextSawedOrderWorkplace = nextSawedOrder.getWorkplace();
                    nextSawedOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(Carpenter.Group.C));
                    simulation.addEvent(new FurnitureSoakingStartEvent(simulation, executionTime, nextSawedOrderWorkplace));
                }
            }
        }
    }

    private static class FurnitureAssemblingStartEvent extends FurnitureSimulationEvent {
        public FurnitureAssemblingStartEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime, workplace);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            Order order = workplace.getOrder();
            order.setState(Order.State.ASSEMBLING);

            Carpenter carpenter = workplace.getCarpenter();
            carpenter.setState(Carpenter.State.WORKING, executionTime);

            double endTime = simulation.getNextActionTime(order);

            endTime += executionTime + simulation.getCarpenterMoveTime(carpenter, workplace);

            carpenter.setPosition(Carpenter.Position.WORKPLACE);
            carpenter.setWorkplace(workplace);
            simulation.addEvent(new FurnitureAssemblingEndEvent(simulation, endTime, workplace));
        }
    }

    private static class FurnitureAssemblingEndEvent extends FurnitureSimulationEvent {
        public FurnitureAssemblingEndEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime, workplace);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            workplace.getCarpenter().setState(Carpenter.State.FREE, executionTime);
            workplace.unassignCarpenter();

            Order order = workplace.getOrder();

            if (order.getType() == Order.Type.WARDROBE) {
                order.setState(Order.State.ASSEMBLED);

                Carpenter freeCarpenter = simulation.getFreeCarpenterFromGroup(Carpenter.Group.C);
                if (freeCarpenter != null) {
                    workplace.assignCarpenter(freeCarpenter);
                    simulation.addEvent(new FurnitureFittingsInstallationStartEvent(simulation, executionTime, workplace));
                } else {
                    simulation.addOrderToQueue(order);
                }
            } else {
                order.setState(Order.State.DONE);
                simulation.incrementNumberOfDoneOrders();
                workplace.unassignOrder();
                simulation.experimentStatistics.orderWorkingTime.addValue(executionTime - order.getArrivalTime());
            }

            Order nextSoakedOrder = simulation.getNextOrderFromQueue(Order.State.SOAKED);
            if (nextSoakedOrder != null) {
                Workplace nextSoakedOrderWorkplace = nextSoakedOrder.getWorkplace();
                nextSoakedOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(Carpenter.Group.B));
                simulation.addEvent(new FurnitureAssemblingStartEvent(simulation, executionTime, nextSoakedOrderWorkplace));
            }
        }
    }

    private static class FurnitureFittingsInstallationStartEvent extends FurnitureSimulationEvent {
        public FurnitureFittingsInstallationStartEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime, workplace);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            Carpenter carpenter = workplace.getCarpenter();
            carpenter.setState(Carpenter.State.WORKING, executionTime);

            Order order = workplace.getOrder();
            order.setState(Order.State.FITTINGS_INSTALLATION);

            if (order.getType() != Order.Type.WARDROBE) {
                throw new IllegalStateException("Fittings are not installing on this type of furniture : " + order.getType());
            }

            double endTime = executionTime + simulation.getNextActionTime(order) + simulation.getCarpenterMoveTime(carpenter, workplace);

            carpenter.setPosition(Carpenter.Position.WORKPLACE);
            carpenter.setWorkplace(workplace);
            simulation.addEvent(new FurnitureFittingsInstallationEndEvent(simulation, endTime, workplace));
        }
    }

    private static class FurnitureFittingsInstallationEndEvent extends FurnitureSimulationEvent {
        public FurnitureFittingsInstallationEndEvent(FurnitureSimulation simulation, double executionTime, Workplace workplace) {
            super(simulation, executionTime, workplace);
        }

        @Override
        public void execute() {
            FurnitureSimulation simulation = (FurnitureSimulation) super.simulation;

            workplace.getCarpenter().setState(Carpenter.State.FREE, executionTime);
            Order order = workplace.getOrder();
            order.setState(Order.State.DONE);
            simulation.incrementNumberOfDoneOrders();
            workplace.unassignCarpenter();
            workplace.unassignOrder();
            simulation.experimentStatistics.orderWorkingTime.addValue(executionTime - order.getArrivalTime());

            Order nextAssembledOrder = simulation.getNextOrderFromQueue(Order.State.ASSEMBLED);
            if (nextAssembledOrder != null) {
                Workplace nextAssembledOrderWorkplace = nextAssembledOrder.getWorkplace();
                nextAssembledOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(Carpenter.Group.C));
                simulation.addEvent(new FurnitureFittingsInstallationStartEvent(simulation, executionTime, nextAssembledOrderWorkplace));
            } else {
                Order nextSawedOrder = simulation.getNextOrderFromQueue(Order.State.SAWED);
                if (nextSawedOrder != null) {
                    Workplace nextSawedOrderWorkplace = nextSawedOrder.getWorkplace();
                    nextSawedOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(Carpenter.Group.C));
                    simulation.addEvent(new FurnitureSoakingStartEvent(simulation, executionTime, nextSawedOrderWorkplace));
                }
            }
        }
    }
}
