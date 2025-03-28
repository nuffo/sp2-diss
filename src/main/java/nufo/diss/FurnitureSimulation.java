package nufo.diss;

import nufo.diss.generators.*;

import java.util.*;
import java.util.stream.Stream;

public class FurnitureSimulation extends EventSimulation{
    private Queue<Order> newOrdersQueue;
    private Queue<Order> sawedOrdersQueue;
    private Queue<Order> soakedOrdersQueue;
    private Queue<Order> assembledOrdersQueue;
    private final int carpenterGroupASize;
    private final int carpenterGroupBSize;
    private final int carpenterGroupCSize;
    private List<Carpenter> carpenterGroupA;
    private List<Carpenter> carpenterGroupB;
    private List<Carpenter> carpenterGroupC;
    private List<Workplace> workplaces;
    private Random orderTypeProbabilityGenerator;
    private ExponentialGenerator orderArrivalGenerator;
    private TriangularGenerator timeToMoveBetweenWorkplaceAndWarehouseGenerator;
    private TriangularGenerator timeToPrepareMaterialInWarehouseGenerator;
    private TriangularGenerator timeToMoveBetweenWorkplacesGenerator;
    private EmpiricalGenerator timeToSawingTablePartsGenerator;
    private UniformGenerator timeToSoakingTablePartsGenerator;
    private UniformGenerator timeToAssemblyTableGenerator;
    private UniformGenerator timeToSawingChairPartsGenerator;
    private UniformGenerator timeToSoakingChairPartsGenerator;
    private UniformGenerator timeToAssemblyChairGenerator;
    private UniformGenerator timeToSawingWardrobePartsGenerator;
    private UniformGenerator timeToSoakingWardrobePartsGenerator;
    private UniformGenerator timeToAssemblyWardrobeGenerator;
    private UniformGenerator timeToFittingsInstallationWardrobeGenerator;

    private int numberOfArrivedOrders;
    private int numberOfDoneOrders;

    protected FurnitureSimulation(
            int numberOfReplications,
            int skipReplicationsPercentage,
            ExecutionMode executionMode,
            double maxTime,
            int carpenterGroupASize,
            int carpenterGroupBSize,
            int carpenterGroupCSize
    ) {
        super(numberOfReplications, skipReplicationsPercentage, executionMode, maxTime);

        this.carpenterGroupASize = carpenterGroupASize;
        this.carpenterGroupBSize = carpenterGroupBSize;
        this.carpenterGroupCSize = carpenterGroupCSize;
    }

    public static class Carpenter {
        private State state = State.FREE;
        private Position position = Position.WAREHOUSE;
        private Workplace workplace;
        private final Group group;
        public static int carpenterId = 0;
        private final int id = ++carpenterId;

        Carpenter(Group group) {
            this.group = group;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
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

        public static void resetCarpenterId() {
            carpenterId = 0;
        }

        public enum State {
            FREE,
            BUSY
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
            return "Carpenter( " +
                    "state: " + state +
                    ", position: " + position +
                    (workplace != null ? ", workplaceId: " + workplace.getId() : "") +
                    ", group: " + group +
                    " )";
        }
    }

    public static class FurnitureSimulationData extends SimulationData {
        private final int numberOfArrivedOrders;
        private final int numberOfDoneOrders;
        private final int numberOfNewOrdersInQueue;
        private final int numberOfSawedOrdersInQueue;
        private final int numberOfSoakedOrdersInQueue;
        private final int numberOfAssembledOrdersInQueue;
        private final String[] workplaces;
        private final String[] carpenters;

        FurnitureSimulationData(
                double currentTime,
                State state,
                int numberOfArrivedOrders,
                int numberOfDoneOrders,
                int numberOfNewOrdersInQueue,
                int numberOfSawedOrdersInQueue,
                int numberOfSoakedOrdersInQueue,
                int numberOfAssembledOrdersInQueue,
                String[] workplaces,
                String[] carpenters
        ) {
            super(currentTime, state);
            this.numberOfArrivedOrders = numberOfArrivedOrders;
            this.numberOfDoneOrders = numberOfDoneOrders;
            this.numberOfNewOrdersInQueue = numberOfNewOrdersInQueue;
            this.numberOfSawedOrdersInQueue = numberOfSawedOrdersInQueue;
            this.numberOfSoakedOrdersInQueue = numberOfSoakedOrdersInQueue;
            this.numberOfAssembledOrdersInQueue = numberOfAssembledOrdersInQueue;
            this.workplaces = workplaces;
            this.carpenters = carpenters;
        }

        public int getNumberOfArrivedOrders() {
            return numberOfArrivedOrders;
        }

        public int getNumberOfDoneOrders() {
            return numberOfDoneOrders;
        }

        public int getNumberOfNewOrdersInQueue() {
            return numberOfNewOrdersInQueue;
        }

        public int getNumberOfSawedOrdersInQueue() {
            return numberOfSawedOrdersInQueue;
        }

        public int getNumberOfSoakedOrdersInQueue() {
            return numberOfSoakedOrdersInQueue;
        }

        public int getNumberOfAssembledOrdersInQueue() {
            return numberOfAssembledOrdersInQueue;
        }

        public String[] getWorkplaces() {
            return workplaces;
        }

        public String[] getCarpenters() {
            return carpenters;
        }
    }

    public static class Order {
        private final Type type;
        private State state;
        private Workplace workplace;

        Order(Type type) {
            this.type = type;
            this.state = State.NEW;
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
            DONE
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
            carpenter.setWorkplace(this);
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
            return "Workplace( id:"+id+", order: "+(order != null ? "Order( type: "+order.getType()+", state: "+order.getState()+" )" : "FREE")+(carpenter != null ? ", carpenter: Carpenter( group: "+carpenter.getGroup()+", state: "+carpenter.getState()+" )" : "")+" )";
        }
    }

    @Override
    protected void notifyStateChange() {
        if (dataConsumer != null) {
            String[] workplaces = this.workplaces.stream().map(Workplace::toString).toArray(String[]::new);

            String[] carpenters = Stream.of(carpenterGroupA, carpenterGroupB, carpenterGroupC)
                    .flatMap(List::stream)
                    .map(Carpenter::toString)
                    .toArray(String[]::new);

            dataConsumer.accept(new FurnitureSimulationData(
                    currentTime,
                    state,
                    numberOfArrivedOrders,
                    numberOfDoneOrders,
                    newOrdersQueue.size(),
                    sawedOrdersQueue.size(),
                    soakedOrdersQueue.size(),
                    assembledOrdersQueue.size(),
                    workplaces,
                    carpenters
            ));
        }
    }

    @Override
    protected void beforeSimulation() {
        orderArrivalGenerator = new ExponentialGenerator(2.0 / 3600.0);
        orderTypeProbabilityGenerator = new Random(SeedGenerator.getInstance().nextInt());

        timeToMoveBetweenWorkplaceAndWarehouseGenerator = new TriangularGenerator(60, 480, 120);
        timeToPrepareMaterialInWarehouseGenerator = new TriangularGenerator(300, 900, 500);
        timeToMoveBetweenWorkplacesGenerator = new TriangularGenerator(120, 500, 150);

        timeToSawingTablePartsGenerator = new EmpiricalGenerator(
                Arrays.asList(10.0 * 60, 25.0 * 60),
                Arrays.asList(25.0 * 60, 50.0 * 60),
                Arrays.asList(0.6, 0.4),
                Generator.Mode.CONTINUOUS
        );
        timeToSoakingTablePartsGenerator = new UniformGenerator(200 * 60, 610 * 60, Generator.Mode.CONTINUOUS);
        timeToAssemblyTableGenerator = new UniformGenerator(30 * 60, 60 * 60, Generator.Mode.CONTINUOUS);
        timeToSawingChairPartsGenerator = new UniformGenerator(12 * 60, 16 * 60, Generator.Mode.CONTINUOUS);
        timeToSoakingChairPartsGenerator = new UniformGenerator(210 * 60, 540 * 60, Generator.Mode.CONTINUOUS);
        timeToAssemblyChairGenerator = new UniformGenerator(14 * 60, 24 * 60, Generator.Mode.CONTINUOUS);
        timeToSawingWardrobePartsGenerator = new UniformGenerator(15 * 60, 80 * 60, Generator.Mode.CONTINUOUS);
        timeToSoakingWardrobePartsGenerator = new UniformGenerator(600 * 60, 700 * 60, Generator.Mode.CONTINUOUS);
        timeToAssemblyWardrobeGenerator = new UniformGenerator(35 * 60, 75 * 60, Generator.Mode.CONTINUOUS);
        timeToFittingsInstallationWardrobeGenerator = new UniformGenerator(15 * 60, 25 * 60, Generator.Mode.CONTINUOUS);

        newOrdersQueue = new LinkedList<>();
        sawedOrdersQueue = new LinkedList<>();
        soakedOrdersQueue = new LinkedList<>();
        assembledOrdersQueue = new LinkedList<>();
        workplaces = new LinkedList<>();

        carpenterGroupA = new LinkedList<>();
        for (int i = 0; i < carpenterGroupASize; i++) {
            carpenterGroupA.add(new Carpenter(Carpenter.Group.A));
        }

        carpenterGroupB = new LinkedList<>();
        for (int i = 0; i < carpenterGroupBSize; i++) {
            carpenterGroupB.add(new Carpenter(Carpenter.Group.B));
        }

        carpenterGroupC = new LinkedList<>();
        for (int i = 0; i < carpenterGroupCSize; i++) {
            carpenterGroupC.add(new Carpenter(Carpenter.Group.C));
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
        Workplace.resetWorkplaceId();
        Carpenter.resetCarpenterId();
        addEvent(new OrderArrivalEvent(this, getNextOrderArrivalTime()));
    }

    @Override
    protected void afterExperiment() {

    }

    public Carpenter getFreeCarpenterFromGroup(List<Carpenter> group) {
        for (Carpenter carpenter : group) {
            if (carpenter.getState() == Carpenter.State.FREE) {
                return carpenter;
            }
        }
        return null;
    }

    public void addNewOrderToQueue(Order order) {
        if (!newOrdersQueue.add(order)) {
            throw new RuntimeException("Something went wrong while adding to new orders queue.");
        }
    }

    public Order getNextNewOrderFromQueue() {
        return newOrdersQueue.poll();
    }

    public void addSawedOrderToQueue(Order order) {
        if (!sawedOrdersQueue.add(order)) {
            throw new RuntimeException("Something went wrong while adding to prepared orders queue.");
        }
    }

    public Order getNextSawedOrderFromQueue() {
        return sawedOrdersQueue.poll();
    }

    public void addSoakedOrderToQueue(Order order) {
        if (!soakedOrdersQueue.add(order)) {
            throw new RuntimeException("Something went wrong while adding to soaked orders queue.");
        }
    }

    public Order getNextSoakedOrderFromQueue() {
        return soakedOrdersQueue.poll();
    }

    public void addAssembledOrderToQueue(Order order) {
        if (!assembledOrdersQueue.add(order)) {
            throw new RuntimeException("Something went wrong while adding to assembled orders queue.");
        }
    }

    public Order getNextAssembledOrderFromQueue() {
        return assembledOrdersQueue.poll();
    }

    public double getNextOrderArrivalTime() {
        if (orderArrivalGenerator == null) {
            throw new IllegalStateException("Order arrival generator not set.");
        }
        return currentTime + orderArrivalGenerator.nextDouble();
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

    public double getNextTimeToSawingTableParts() {
        if (timeToSawingTablePartsGenerator == null) {
            throw new IllegalStateException("Time to sawing table parts generator not set.");
        }
        return timeToSawingTablePartsGenerator.nextDouble();
    }

    public double getNextTimeToSoakingTableParts() {
        if (timeToSoakingTablePartsGenerator == null) {
            throw new IllegalStateException("Time to soaking table parts generator not set.");
        }
        return timeToSoakingTablePartsGenerator.nextDouble();
    }

    public double getNextTimeToAssemblyTableParts() {
        if (timeToAssemblyTableGenerator == null) {
            throw new IllegalStateException("Time to assembly table parts generator not set.");
        }
        return timeToAssemblyTableGenerator.nextDouble();
    }

    public double getNextTimeToSawingChairParts() {
        if (timeToSawingChairPartsGenerator == null) {
            throw new IllegalStateException("Time to sawing chair parts generator not set.");
        }
        return timeToSawingChairPartsGenerator.nextDouble();
    }

    public double getNextTimeToSoakingChairParts() {
        if (timeToSoakingChairPartsGenerator == null) {
            throw new IllegalStateException("Time to soaking chair parts generator not set.");
        }
        return timeToSoakingChairPartsGenerator.nextDouble();
    }

    public double getNextTimeToAssemblyChairParts() {
        if (timeToAssemblyChairGenerator == null) {
            throw new IllegalStateException("Time to assembly chair parts generator not set.");
        }
        return timeToAssemblyChairGenerator.nextDouble();
    }

    public double getNextTimeToSawingWardrobeParts() {
        if (timeToSawingWardrobePartsGenerator == null) {
            throw new IllegalStateException("Time to sawing wardrobe parts generator not set.");
        }
        return timeToSawingWardrobePartsGenerator.nextDouble();
    }

    public double getNextTimeToSoakingWardrobeParts() {
        if (timeToSoakingWardrobePartsGenerator == null) {
            throw new IllegalStateException("Time to soaking wardrobe parts generator not set.");
        }
        return timeToSoakingWardrobePartsGenerator.nextDouble();
    }

    public double getNextTimeToAssemblyWardrobeParts() {
        if (timeToAssemblyWardrobeGenerator == null) {
            throw new IllegalStateException("Time to assembly wardrobe parts generator not set.");
        }
        return timeToAssemblyWardrobeGenerator.nextDouble();
    }

    public double getNextTimeToFittingsInstallationWardrobeParts() {
        if (timeToFittingsInstallationWardrobeGenerator == null) {
            throw new IllegalStateException("Time to fitting installation wardrobe parts generator not set.");
        }
        return timeToFittingsInstallationWardrobeGenerator.nextDouble();
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

    public List<Carpenter> getCarpenterGroupA() {
        return carpenterGroupA;
    }

    public List<Carpenter> getCarpenterGroupB() {
        return carpenterGroupB;
    }

    public List<Carpenter> getCarpenterGroupC() {
        return carpenterGroupC;
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

            Order order = new Order(simulation.getNextOrderType());
            simulation.incrementNumberOfArrivedOrders();

            Carpenter freeCarpenter = simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupA());
            if (freeCarpenter != null) {
                Workplace workplace = simulation.getFreeWorkplace();
                workplace.assignOrder(order);
                workplace.assignCarpenter(freeCarpenter);
                simulation.addEvent(new FurnitureSawingStartEvent(simulation, simulation.getCurrentTime(), workplace));
            } else {
                simulation.addNewOrderToQueue(order);
            }

            simulation.addEvent(new OrderArrivalEvent(simulation, simulation.getNextOrderArrivalTime()));
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
            carpenter.setState(Carpenter.State.BUSY);

            Order order = workplace.getOrder();
            order.setState(Order.State.SAWING);

            double sawingTime = switch (order.getType()) {
                case TABLE -> simulation.getNextTimeToSawingTableParts();
                case CHAIR -> simulation.getNextTimeToSawingChairParts();
                case WARDROBE -> simulation.getNextTimeToSawingWardrobeParts();
            };

            double time = simulation.getCurrentTime() + simulation.getNextTimeToMoveBetweenWorkplaceAndWarehouse() + simulation.getNextTimeToPrepareMaterialInWarehouse() + sawingTime;

            if (carpenter.getPosition() == Carpenter.Position.WORKPLACE) {
                time += simulation.getNextTimeToMoveBetweenWorkplaceAndWarehouse();
            }

            carpenter.setPosition(Carpenter.Position.WORKPLACE);

            simulation.addEvent(new FurnitureSawingEndEvent(simulation, time, workplace));
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
            workplace.getCarpenter().setState(Carpenter.State.FREE);
            workplace.unassignCarpenter();

            Carpenter freeCarpenter = simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupC());
            if (freeCarpenter != null) {
                workplace.assignCarpenter(freeCarpenter);
                simulation.addEvent(new FurnitureSoakingStartEvent(simulation, simulation.getCurrentTime(), workplace));
            } else {
                simulation.addSawedOrderToQueue(order);
            }

            Order nextNewOrder = simulation.getNextNewOrderFromQueue();
            if (nextNewOrder != null) {
                Workplace freeWorkplace = simulation.getFreeWorkplace();
                freeWorkplace.assignOrder(nextNewOrder);
                freeWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupA()));
                simulation.addEvent(new FurnitureSawingStartEvent(simulation, simulation.getCurrentTime(), freeWorkplace));
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
            carpenter.setState(Carpenter.State.BUSY);

            Order order = workplace.getOrder();
            order.setState(Order.State.SOAKING);

            double endTime = switch (order.getType()) {
                case TABLE -> simulation.getNextTimeToSoakingTableParts();
                case CHAIR -> simulation.getNextTimeToSoakingChairParts();
                case WARDROBE -> simulation.getNextTimeToSoakingWardrobeParts();
            };

            endTime += simulation.getCurrentTime() + simulation.getCarpenterMoveTime(carpenter, workplace);

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

            workplace.getCarpenter().setState(Carpenter.State.FREE);

            Order order = workplace.getOrder();
            order.setState(Order.State.SOAKED);
            workplace.unassignCarpenter();

            Carpenter freeCarpenter = simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupB());
            if (freeCarpenter != null) {
                workplace.assignCarpenter(freeCarpenter);
                simulation.addEvent(new FurnitureAssemblingStartEvent(simulation, simulation.getCurrentTime(), workplace));
            } else {
                simulation.addSoakedOrderToQueue(order);
            }

            Order nextAssembledOrder = simulation.getNextAssembledOrderFromQueue();
            if (nextAssembledOrder != null) {
                Workplace nextAssembledOrderWorkplace = nextAssembledOrder.getWorkplace();
                nextAssembledOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupC()));
                simulation.addEvent(new FurnitureFittingsInstallationStartEvent(simulation, simulation.getCurrentTime(), nextAssembledOrderWorkplace));
            } else {
                Order nextSawedOrder = simulation.getNextSawedOrderFromQueue();
                if (nextSawedOrder != null) {
                    Workplace nextSawedOrderWorkplace = nextSawedOrder.getWorkplace();
                    nextSawedOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupC()));
                    simulation.addEvent(new FurnitureSoakingStartEvent(simulation, simulation.getCurrentTime(), nextSawedOrderWorkplace));
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
            carpenter.setState(Carpenter.State.BUSY);

            double endTime = switch (order.getType()) {
                case TABLE -> simulation.getNextTimeToAssemblyTableParts();
                case CHAIR -> simulation.getNextTimeToAssemblyChairParts();
                case WARDROBE -> simulation.getNextTimeToAssemblyWardrobeParts();
            };

            endTime += simulation.getCurrentTime() + simulation.getCarpenterMoveTime(carpenter, workplace);

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

            workplace.getCarpenter().setState(Carpenter.State.FREE);
            workplace.unassignCarpenter();

            Order order = workplace.getOrder();

            if (order.getType() == Order.Type.WARDROBE) {
                order.setState(Order.State.ASSEMBLED);

                Carpenter freeCarpenter = simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupC());
                if (freeCarpenter != null) {
                    workplace.assignCarpenter(freeCarpenter);
                    simulation.addEvent(new FurnitureFittingsInstallationStartEvent(simulation, simulation.getCurrentTime(), workplace));
                } else {
                    simulation.addAssembledOrderToQueue(order);
                }
            } else {
                order.setState(Order.State.DONE);
                simulation.incrementNumberOfDoneOrders();
                workplace.unassignOrder();
            }

            Order nextSoakedOrder = simulation.getNextSoakedOrderFromQueue();
            if (nextSoakedOrder != null) {
                Workplace nextSoakedOrderWorkplace = nextSoakedOrder.getWorkplace();
                nextSoakedOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupB()));
                simulation.addEvent(new FurnitureAssemblingStartEvent(simulation, simulation.getCurrentTime(), nextSoakedOrderWorkplace));
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
            carpenter.setState(Carpenter.State.BUSY);

            Order order = workplace.getOrder();
            order.setState(Order.State.FITTINGS_INSTALLATION);

            if (order.getType() != Order.Type.WARDROBE) {
                throw new IllegalStateException("Fittings are not installing on this type of furniture : " + order.getType());
            }

            double endTime = simulation.getCurrentTime() + simulation.getNextTimeToFittingsInstallationWardrobeParts() + simulation.getCarpenterMoveTime(carpenter, workplace);

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

            workplace.getCarpenter().setState(Carpenter.State.FREE);
            workplace.getOrder().setState(Order.State.DONE);
            simulation.incrementNumberOfDoneOrders();
            workplace.unassignCarpenter();
            workplace.unassignOrder();

            Order nextAssembledOrder = simulation.getNextAssembledOrderFromQueue();
            if (nextAssembledOrder != null) {
                Workplace nextAssembledOrderWorkplace = nextAssembledOrder.getWorkplace();
                nextAssembledOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupC()));
                simulation.addEvent(new FurnitureFittingsInstallationStartEvent(simulation, simulation.getCurrentTime(), nextAssembledOrderWorkplace));
            } else {
                Order nextSawedOrder = simulation.getNextSawedOrderFromQueue();
                if (nextSawedOrder != null) {
                    Workplace nextSawedOrderWorkplace = nextSawedOrder.getWorkplace();
                    nextSawedOrderWorkplace.assignCarpenter(simulation.getFreeCarpenterFromGroup(simulation.getCarpenterGroupC()));
                    simulation.addEvent(new FurnitureSoakingStartEvent(simulation, simulation.getCurrentTime(), nextSawedOrderWorkplace));
                }
            }
        }
    }
}
