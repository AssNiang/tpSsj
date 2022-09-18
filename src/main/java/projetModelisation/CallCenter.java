package projetModelisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.stat.TallyStore;

public class CallCenter {

    RandomVariateGen genArrivalTime1, genArrivalTime2;

    RandomVariateGen genServiceTimeC1A1, genServiceTimeC1A2,
            genServiceTimeC2A1, genServiceTimeC2A2;

    RandomVariateGen genPatienceTime1, genPatienceTime2;

    LinkedList<Call> listWaitingCalls1 = new LinkedList<>();
    LinkedList<Call> listWaitingCalls2 = new LinkedList<>();

    int nAgents1, nAgents2;
    LinkedList<Agent> listAgents1 = new LinkedList<>();
    LinkedList<Agent> listAgents2 = new LinkedList<>();
    LinkedList<Agent> listFreeAgents1 = new LinkedList<>();
    LinkedList<Agent> listFreeAgents2 = new LinkedList<>();
    /*
    LinkedList<Agent> listBusyAgents1 = new LinkedList<Agent>();
    LinkedList<Agent> listBusyAgents2 = new LinkedList<Agent>();
     */
    int nArrivals1, nArrivals2;
    TallyStore arrivalsCollector1 = new TallyStore();
    TallyStore arrivalsCollector2 = new TallyStore();

    int nAbandons1, nAbandons2;
    TallyStore abandonsCollector1 = new TallyStore();
    TallyStore abandonsCollector2 = new TallyStore();

    int goodWaitingTimesThreshold, nGoodWaitingTimes1, nGoodWaitingTimes2;
    TallyStore goodWaitingTimesCollector1 = new TallyStore();
    TallyStore goodWaitingTimesCollector2 = new TallyStore();

    int nbDays; // number of days
    int nbHoursPerDay; // number of hours per day (T)

    int dayIndex = 0;

    final double MINUTE = 60.0;
    final double HOUR = 3600.0;

    public CallCenter(
            double lambda1, double lambda2,
            double mu11, double mu12, double mu21, double mu22,
            double nu1, double nu2,
            int n1, int n2,
            int s,
            int T,
            int n
    ) {
        this.genArrivalTime1 = new ExponentialGen(new MRG32k3a(), lambda1);
        this.genArrivalTime2 = new ExponentialGen(new MRG32k3a(), lambda2);
        this.genServiceTimeC1A1 = new ExponentialGen(new MRG32k3a(), mu11);
        this.genServiceTimeC1A2 = new ExponentialGen(new MRG32k3a(), mu12);
        this.genServiceTimeC2A1 = new ExponentialGen(new MRG32k3a(), mu21);
        this.genServiceTimeC2A2 = new ExponentialGen(new MRG32k3a(), mu22);
        this.genPatienceTime1 = new ExponentialGen(new MRG32k3a(), nu1);
        this.genPatienceTime2 = new ExponentialGen(new MRG32k3a(), nu2);
        this.nAgents1 = n1;
        this.nAgents2 = n2;
        this.goodWaitingTimesThreshold = s;
        this.nbHoursPerDay = T;
        this.nbDays = n;

        // start the simulation
        simulate();
    }

    private class Agent {

        int agentType;
        ArrayList<Double> listOccupationRates = new ArrayList<>();
        ArrayList<Call> callsResponded1 = new ArrayList<>();
        ArrayList<Call> callsResponded2 = new ArrayList<>();

        public Agent(int agentType) {
            this.agentType = agentType;
            for (int i = 0; i < nbDays; i++) {
                listOccupationRates.add(i, 0.0);
            }
        }
    }

    private class Call {

        int callType;
        double arrivalTime;
        double serviceTime;
        double patienceTime;
        Agent agentWhoResponded;

        public Call() {
        }
    }

    private class Arrival extends Event {

        int type;

        public Arrival(int type) {
            this.type = type;
        }

        @Override
        public void actions() {
            if (this.type == 1) {
                new Arrival(type).schedule(genArrivalTime1.nextDouble() * MINUTE);

                nArrivals1++;

                Call call = new Call();
                call.callType = this.type;
                call.arrivalTime = sim.time();
                call.patienceTime = genPatienceTime1.nextDouble() * MINUTE;

                new Abandon(call).schedule(call.patienceTime);

                switch (getAgent(call.callType)) {
                    case 1 -> {
                        // an agent 1 took the call
                        Agent agent1 = (Agent) listFreeAgents1.removeFirst();
                        //listBusyAgents1.add(agent1);
                        agent1.callsResponded1.add(call);
                        call.agentWhoResponded = agent1;
                        call.serviceTime = genServiceTimeC1A1.nextDouble() * MINUTE;

                        new EndOfCall(call).schedule(call.serviceTime);

                        nGoodWaitingTimes1++;
                    }
                    case 2 -> {
                        // an agent 2 took the call
                        Agent agent2 = (Agent) listFreeAgents2.removeFirst();
                        //listBusyAgents2.add(agent2);
                        agent2.callsResponded1.add(call);
                        call.agentWhoResponded = agent2;
                        call.serviceTime = genServiceTimeC1A2.nextDouble() * MINUTE;

                        new EndOfCall(call).schedule(call.serviceTime);

                        nGoodWaitingTimes1++;
                    }
                    default -> // no agent can take the call
                        listWaitingCalls1.addLast(call);
                }
            } else if (this.type == 2) {
                new Arrival(type).schedule(genArrivalTime2.nextDouble() * MINUTE);

                nArrivals2++;

                Call call = new Call();
                call.callType = this.type;
                call.arrivalTime = sim.time();
                call.patienceTime = genPatienceTime2.nextDouble() * MINUTE;

                new Abandon(call).schedule(call.patienceTime);

                switch (getAgent(call.callType)) {
                    case 1 -> {
                        // an agent 1 took the call
                        Agent agent1 = (Agent) listFreeAgents1.removeFirst();
                        //listBusyAgents1.add(agent1);
                        agent1.callsResponded2.add(call);
                        call.agentWhoResponded = agent1;
                        call.serviceTime = genServiceTimeC2A1.nextDouble() * MINUTE;

                        new EndOfCall(call).schedule(call.serviceTime);

                        nGoodWaitingTimes2++;
                    }
                    case 2 -> {
                        // an agent 2 took the call
                        Agent agent2 = (Agent) listFreeAgents2.removeFirst();
                        //listBusyAgents2.add(agent2);
                        agent2.callsResponded2.add(call);
                        call.agentWhoResponded = agent2;
                        call.serviceTime = genServiceTimeC2A2.nextDouble() * MINUTE;

                        new EndOfCall(call).schedule(call.serviceTime);

                        nGoodWaitingTimes2++;
                    }
                    default -> // no agent can take the call
                        listWaitingCalls2.addLast(call);
                }
            }
        }

    }

    private int getAgent(int callType) {
        if (callType == 1) {
            if (!listFreeAgents1.isEmpty()) {
                return 1;
            } else if (!listFreeAgents2.isEmpty()) {
                return 2;
            }
        } else if (callType == 2) {
            if (!listFreeAgents2.isEmpty()) {
                return 2;
            } else if (!listFreeAgents1.isEmpty()) {
                return 1;
            }
        }
        return 0;
    }

    class Abandon extends Event {

        Call call;

        public Abandon(Call call) {
            this.call = call;
        }

        @Override
        public void actions() {
            if (listWaitingCalls1.contains(call)) {
                listWaitingCalls1.remove(call);
                // on ne nous demande pas le temps d'attente moyenne
                nAbandons1++;
            } else if (listWaitingCalls2.contains(call)) {
                listWaitingCalls2.remove(call);
                nAbandons2++;
            }
        }
    }

    /*
    private int getAnotherCall(int agentType) {
        // to implement later...
        return 0;
    }
     */
    class EndOfCall extends Event {

        Call call;

        public EndOfCall(Call call) {
            this.call = call;
        }

        @Override
        public void actions() {
            Agent agent = this.call.agentWhoResponded;
            agent.listOccupationRates.set(dayIndex, agent.listOccupationRates.get(dayIndex) + this.call.serviceTime); // on est en train de gérer un jour donné.
            // we can maybe remove the call

            switch (agent.agentType) {
                case 1 -> {
                    //listBusyAgents1.remove(agent);

                    if (!listWaitingCalls1.isEmpty()) {
                        // cela veut dire que tous les agents 1 sont occupes
                        Call ca = listWaitingCalls1.removeFirst();
                        ca.agentWhoResponded = agent;
                        agent.callsResponded1.add(ca);
                        // on genere le serviceTime
                        ca.serviceTime = genServiceTimeC1A1.nextDouble() * MINUTE;
                        // on programme la fin du call dans call.serviceTime
                        new EndOfCall(ca).schedule(ca.serviceTime);
                        // on verifie si le temps d'attente est < s pour le garder (1)
                        if (sim.time() - ca.arrivalTime <= goodWaitingTimesThreshold) {
                            nGoodWaitingTimes1++;
                        }
                    } else if (!listWaitingCalls2.isEmpty()) {
                        // cela veut dire que tous les agents 2 sont occupes
                        // donc il prend la tete de file
                        Call ca = listWaitingCalls2.removeFirst();
                        ca.agentWhoResponded = agent;
                        agent.callsResponded2.add(ca);
                        // on genere le serviceTime
                        ca.serviceTime = genServiceTimeC2A1.nextDouble() * MINUTE;
                        // on programme la fin du call dans call.serviceTime
                        new EndOfCall(ca).schedule(ca.serviceTime);
                        // on verifie si le temps d'attente est < s pour le garder (2)
                        if (sim.time() - ca.arrivalTime <= goodWaitingTimesThreshold) {
                            nGoodWaitingTimes2++;
                        }
                    } else {
                        // cela veut dire qu'aucun appel n'est en attente
                        // on le remet a la queue des agents 1 libres.
                        listFreeAgents1.addLast(agent);
                    }
                    break;
                }
                case 2 -> {
                    //listBusyAgents2.remove(agent);

                    if (!listWaitingCalls2.isEmpty()) {
                        // cela veut dire que tous les agents 2 sont occupes
                        // donc il prend la tete de file
                        Call ca = listWaitingCalls2.removeFirst();
                        ca.agentWhoResponded = agent;
                        agent.callsResponded2.add(ca);
                        // on genere le serviceTime
                        ca.serviceTime = genServiceTimeC2A2.nextDouble() * MINUTE;
                        // on programme la fin du call dans call.serviceTime
                        new EndOfCall(ca).schedule(ca.serviceTime);
                        // on verifie si le temps d'attente est < s pour le garder (2)
                        if (sim.time() - ca.arrivalTime <= goodWaitingTimesThreshold) {
                            nGoodWaitingTimes2++;
                        }
                    } else if (!listWaitingCalls1.isEmpty()) {
                        // cela veut dire que tous les agents 1 sont occupes
                        Call ca = listWaitingCalls1.removeFirst();
                        ca.agentWhoResponded = agent;
                        agent.callsResponded1.add(ca);
                        // on genere le serviceTime
                        ca.serviceTime = genServiceTimeC1A2.nextDouble() * MINUTE;
                        // on programme la fin du call dans call.serviceTime
                        new EndOfCall(ca).schedule(ca.serviceTime);
                        // on verifie si le temps d'attente est < s pour le garder (1)
                        if (sim.time() - ca.arrivalTime <= goodWaitingTimesThreshold) {
                            nGoodWaitingTimes1++;
                        }
                    } else {
                        // cela veut dire qu'aucun appel n'est en attente
                        // on le remet a la queue des agents 1 libres.
                        listFreeAgents2.addLast(agent);
                    }
                    break;
                }

            }

        }

    }

    private void simulate() {
        Sim.init();

        new EndOfDay().schedule(nbHoursPerDay * HOUR);
        new Arrival(1).schedule(genArrivalTime1.nextDouble() * MINUTE);
        new Arrival(2).schedule(genArrivalTime2.nextDouble() * MINUTE);

        for (int i = 0; i < nAgents1; i++) {
            listAgents1.add(i, new Agent(1));
        }
        for (int i = 0; i < nAgents2; i++) {
            listAgents2.add(i, new Agent(2));
        }
        
        listFreeAgents1.addAll(listAgents1);
        listFreeAgents2.addAll(listAgents2);

        Sim.start();
    }

    private class EndOfDay extends Event {

        @Override
        public void actions() {
            // update variables, collectors ...
            arrivalsCollector1.add(nArrivals1);
            arrivalsCollector2.add(nArrivals2);
            nArrivals1 = nArrivals2 = 0;

            abandonsCollector1.add(nAbandons1);
            abandonsCollector2.add(nAbandons2);
            nAbandons1 = nAbandons2 = 0;

            goodWaitingTimesCollector1.add(nGoodWaitingTimes1);
            goodWaitingTimesCollector2.add(nGoodWaitingTimes2);
            nGoodWaitingTimes1 = nGoodWaitingTimes2 = 0;

            listWaitingCalls1.clear();
            listWaitingCalls2.clear();

//            new Arrival(1).schedule(genArrivalTime1.nextDouble() * MINUTE);
//            new Arrival(2).schedule(genArrivalTime2.nextDouble() * MINUTE);

            listFreeAgents1.clear();
            listFreeAgents2.clear();
            listFreeAgents1.addAll(listAgents1);
            listFreeAgents2.addAll(listAgents2);

            if (++dayIndex < nbDays) {
                new EndOfDay().schedule(nbHoursPerDay);
                new EndOfDay().schedule(nbHoursPerDay * HOUR);
            } else {
                new EndOfSimulation().schedule(0.0);
            }
        }
    }

    private class EndOfSimulation extends Event {

        @Override
        public void actions() {
            Sim.stop();
        }

    }

    public static void main(String args[]) {
        // TODO code application logic here
        double lambda1 = 6.0, lambda2 = 0.6;
        double mu11 = 0.20, mu12 = 0.15, mu21 = 0.14, mu22 = 0.18;
        double nu1 = 0.12, nu2 = 0.24;
        int n1 = 31, n2 = 6;
        int s = 20;
        int T = 8;
        int n = 1000;

        CallCenter cc = new CallCenter(lambda1, lambda2, mu11, mu12, mu21, mu22, nu1, nu2, n1, n2, s, T, n);
        
        System.out.println("cc.arrivalsCollector1.report() : ");
        System.out.println(cc.arrivalsCollector1.report());
        System.out.println("cc.arrivalsCollector2.report() : ");
        System.out.println(cc.arrivalsCollector2.report());

        System.out.println("cc.abandonsCollector1.report() : ");
        System.out.println(cc.abandonsCollector1.report());
        System.out.println("cc.abandonsCollector2.report() : ");
        System.out.println(cc.abandonsCollector2.report());

        System.out.println("cc.goodWaitingTimesCollector1.report() : ");
        System.out.println(cc.goodWaitingTimesCollector1.report());
        System.out.println("cc.goodWaitingTimesCollector2.report() : ");
        System.out.println(cc.goodWaitingTimesCollector2.report());
        /*
        System.out.println("cc.abandonsCollector1.getArray() : " + Arrays.toString(cc.abandonsCollector1.getArray()));
        System.out.println("cc.abandonsCollector2.getArray() : " + Arrays.toString(cc.abandonsCollector2.getArray()));
        System.out.println("cc.goodWaitingTimesCollector1.getArray() : " + Arrays.toString(cc.goodWaitingTimesCollector1.getArray()));
        System.out.println("cc.goodWaitingTimesCollector2.getArray() : " + Arrays.toString(cc.goodWaitingTimesCollector2.getArray()));
         */
    }
}

// do not forget filling listFreeAgents with instances of Agent "new Agent()". maybe a loop;
