package projetModelisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import umontreal.ssj.charts.*;
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

    final double HOUR = 3600.0;

    int nbFreeAgents2ToAnswerCall1Threshold;
    int nbFreeAgents1ToAnswerCall2Threshold;

    public CallCenter(
            double lambda1, double lambda2,
            double mu11, double mu12, double mu21, double mu22,
            double nu1, double nu2,
            int n1, int n2,
            int s,
            int T,
            int n,
            int s1,
            int s2) {
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

        this.nbFreeAgents1ToAnswerCall2Threshold = s1;
        this.nbFreeAgents2ToAnswerCall1Threshold = s2;

        for (int i = 0; i < nAgents1; i++) {
            listAgents1.add(i, new Agent(1));
        }
        for (int i = 0; i < nAgents2; i++) {
            listAgents2.add(i, new Agent(2));
        }

        listFreeAgents1.addAll(listAgents1);
        listFreeAgents2.addAll(listAgents2);

        // start the simulation
        simulate();
    }

    private class Agent {

        int agentType;
        ArrayList<Call> callsResponded1 = new ArrayList<>();
        ArrayList<Call> callsResponded2 = new ArrayList<>();
        double[] listBusyness = new double[nbDays];

        public Agent(int agentType) {
            this.agentType = agentType;
        }
    }

    private class Call {

        int callType;
        double arrivalTime;
        double serviceTime;
        double patienceTime;
        Agent agentWhoResponded;

        public Call(int callType) {
            this.callType = callType;
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

                new Arrival(this.type).schedule(genArrivalTime1.nextDouble());

                nArrivals1++;

                Call call = new Call(this.type);
                call.arrivalTime = sim.time();
                call.patienceTime = genPatienceTime1.nextDouble();

                new Abandon(call).schedule(call.patienceTime);

                if (!listFreeAgents1.isEmpty()) {
                    // an agent 1 took the call
                    Agent agent1 = (Agent) listFreeAgents1.removeFirst();
                    agent1.callsResponded1.add(call);
                    call.agentWhoResponded = agent1;
                    call.serviceTime = genServiceTimeC1A1.nextDouble();

                    new EndOfCall(call).schedule(call.serviceTime);

                    nGoodWaitingTimes1++;
                } else if (listFreeAgents2.size() > nbFreeAgents2ToAnswerCall1Threshold) {
                    // an agent 2 took the call
                    Agent agent2 = (Agent) listFreeAgents2.removeFirst();
                    agent2.callsResponded1.add(call);
                    call.agentWhoResponded = agent2;
                    call.serviceTime = genServiceTimeC1A2.nextDouble();

                    new EndOfCall(call).schedule(call.serviceTime);

                    nGoodWaitingTimes1++;
                } else {
                    listWaitingCalls1.addLast(call);
                }

            } else if (this.type == 2) {
                new Arrival(this.type).schedule(genArrivalTime2.nextDouble());

                nArrivals2++;

                Call call = new Call(this.type);
                call.arrivalTime = sim.time();
                call.patienceTime = genPatienceTime2.nextDouble();

                new Abandon(call).schedule(call.patienceTime);

                if (!listFreeAgents2.isEmpty()) {
                    // an agent 2 took the call
                    Agent agent2 = (Agent) listFreeAgents2.removeFirst();
                    agent2.callsResponded2.add(call);
                    call.agentWhoResponded = agent2;
                    call.serviceTime = genServiceTimeC2A2.nextDouble();

                    new EndOfCall(call).schedule(call.serviceTime);

                    nGoodWaitingTimes2++;

                } else if (listFreeAgents1.size() > nbFreeAgents1ToAnswerCall2Threshold) {
                    // an agent 1 took the call
                    Agent agent1 = (Agent) listFreeAgents1.removeFirst();
                    agent1.callsResponded2.add(call);
                    call.agentWhoResponded = agent1;
                    call.serviceTime = genServiceTimeC2A1.nextDouble();

                    new EndOfCall(call).schedule(call.serviceTime);

                    nGoodWaitingTimes2++;
                } else {
                    listWaitingCalls2.addLast(call);
                }
            }
        }

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

    class EndOfCall extends Event {

        Call call;

        public EndOfCall(Call call) {
            this.call = call;
        }

        @Override
        public void actions() {
            Agent agent = this.call.agentWhoResponded;
            agent.listBusyness[dayIndex] += this.call.serviceTime;

            switch (agent.agentType) {
                case 1 -> {

                    if (!listWaitingCalls1.isEmpty()) {
                        Call ca = listWaitingCalls1.removeFirst();
                        ca.agentWhoResponded = agent;
                        agent.callsResponded1.add(ca);
                        ca.serviceTime = genServiceTimeC1A1.nextDouble();
                        new EndOfCall(ca).schedule(ca.serviceTime);
                        if (sim.time() - ca.arrivalTime <= goodWaitingTimesThreshold) {
                            nGoodWaitingTimes1++;
                            //System.out.println(sim.time() - ca.arrivalTime+ " - " + ca.serviceTime);
                        }
                    } else if (!listWaitingCalls2.isEmpty()) {
                        if (listFreeAgents1.size() >= nbFreeAgents1ToAnswerCall2Threshold) {
                            Call ca = listWaitingCalls2.removeFirst();
                            ca.agentWhoResponded = agent;
                            agent.callsResponded2.add(ca);
                            ca.serviceTime = genServiceTimeC2A1.nextDouble();
                            new EndOfCall(ca).schedule(ca.serviceTime);
                            if (sim.time() - ca.arrivalTime <= goodWaitingTimesThreshold) {
                                nGoodWaitingTimes2++;
                            }
                        } else {
                            listFreeAgents1.addLast(agent);
                        }

                    } else {
                        listFreeAgents1.addLast(agent);
                    }
                    break;
                }
                case 2 -> {

                    if (!listWaitingCalls2.isEmpty()) {
                        Call ca = listWaitingCalls2.removeFirst();
                        ca.agentWhoResponded = agent;
                        agent.callsResponded2.add(ca);
                        ca.serviceTime = genServiceTimeC2A2.nextDouble();
                        new EndOfCall(ca).schedule(ca.serviceTime);

                        if (sim.time() - ca.arrivalTime <= goodWaitingTimesThreshold) {
                            nGoodWaitingTimes2++;

                        }
                    } else if (!listWaitingCalls1.isEmpty()) {

                        if (listFreeAgents2.size() >= nbFreeAgents2ToAnswerCall1Threshold) {
                            Call ca = listWaitingCalls1.removeFirst();
                            ca.agentWhoResponded = agent;
                            agent.callsResponded1.add(ca);
                            ca.serviceTime = genServiceTimeC1A2.nextDouble();
                            new EndOfCall(ca).schedule(ca.serviceTime);

                            if (sim.time() - ca.arrivalTime <= goodWaitingTimesThreshold) {
                                nGoodWaitingTimes1++;
                            }
                        } else {
                            listFreeAgents2.addLast(agent);
                        }

                    } else {
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
        new Arrival(1).schedule(genArrivalTime1.nextDouble());
        new Arrival(2).schedule(genArrivalTime2.nextDouble());

        Sim.start();
    }

    private class EndOfDay extends Event {

        @Override
        public void actions() {

            for (Agent ag1 : listAgents1) {
                ag1.listBusyness[dayIndex] /= (nbHoursPerDay * HOUR);
            }
            for (Agent ag2 : listAgents2) {
                ag2.listBusyness[dayIndex] /= (nbHoursPerDay * HOUR);
            }

            arrivalsCollector1.add(nArrivals1);
            arrivalsCollector2.add(nArrivals2);

            abandonsCollector1.add(nAbandons1 * 100.0 / nArrivals1);
            abandonsCollector2.add(nAbandons2 * 100.0 / nArrivals2);
            
            goodWaitingTimesCollector1.add(nGoodWaitingTimes1 * 100.0 / nArrivals1);
            goodWaitingTimesCollector2.add(nGoodWaitingTimes2 * 100.0 / nArrivals2);
            
            nGoodWaitingTimes1 = nGoodWaitingTimes2 = 0;
            nAbandons1 = nAbandons2 = 0;
            nArrivals1 = nArrivals2 = 0;

            listWaitingCalls1.clear();
            listWaitingCalls2.clear();


//            listFreeAgents1.clear();
//            listFreeAgents2.clear();
//            listFreeAgents1.
//            listFreeAgents1.addAll(listAgents1);
//            listFreeAgents2.addAll(listAgents2);
            if (++dayIndex < nbDays) {
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

    // ------------- other methods ---------------------
    public double[] getNbAbandonsByCallType(int callType) {
        return (callType == 1) ? abandonsCollector1.getArray() : abandonsCollector2.getArray();
    }

    public double getNbAbandonsByCallTypeAndByDay(int callType, int day) {
        return getNbAbandonsByCallType(callType)[day - 1];
    }

    public double[] getNbGoodWaitingTimesByCallType(int callType) {
        return (callType == 1) ? goodWaitingTimesCollector1.getArray() : goodWaitingTimesCollector2.getArray();
    }

    public double getNbGoodWaitingTimesByCallTypeAndByDay(int callType, int day) {
        return getNbGoodWaitingTimesByCallType(callType)[day - 1];
    }

    public double[] getBusynessByAgentAndType(int agentType, int numAgent) {
        LinkedList<Agent> agents = (agentType == 1) ? listAgents1 : listAgents2;
        return agents.get(numAgent - 1).listBusyness;
    }

    public double[] getBusynessAverageByAgentType(int agentType) {

        LinkedList<Agent> listAgents = (agentType == 1) ? listAgents1 : listAgents2;
        double[] businessAverageList = new double[nbDays];

        for (int d = 0; d < nbDays; d++) {

            for (int a = 0; a < listAgents.size(); a++) {
                businessAverageList[d] += listAgents.get(a).listBusyness[d];
            }

            businessAverageList[d] /= listAgents.size();
        }
        return businessAverageList;
    }

    // print histograms
    private void printHistogram(int agentType) {
        double[] data = getBusynessAverageByAgentType(agentType);
        String title = "Histogramme des taux d'occupation moyens par jour des agents de type " + agentType;
        String x_label = "Abscisses";
        String y_label = "Ordonnées";
        //System.out.println(Arrays.toString(data));
        HistogramChart chart;
        chart = new HistogramChart(title, x_label, y_label, data);
        HistogramSeriesCollection collec = chart.getSeriesCollection();
        collec.setBins(0, 30);
        double[] bounds = {0.5, 1, 0, 100};
        chart.setManualRange(bounds);
        chart.view(1000, 1000);
    }

    // --------------- main ----------------------
    public static void main(String args[]) {
        // TODO code application logic here
        double lambda1 = 6.0, lambda2 = 0.6;
        double mu11 = 0.20, mu12 = 0.15, mu21 = 0.14, mu22 = 0.18;
        double nu1 = 0.12, nu2 = 0.24;
        int n1 = 31, n2 = 6;
        int s = 20;
        int T = 8;
        int n = 1000;

        int s1, s2;
        s1 = s2 = 0;

        final double MINUTE = 60.0;

        CallCenter cc = new CallCenter(lambda1 / MINUTE, lambda2 / MINUTE, mu11 / MINUTE, mu12 / MINUTE, mu21 / MINUTE, mu22 / MINUTE, nu1, nu2, n1, n2, s, T, n, s1, s2);

        System.out.println(">> Rapport du nombre d'arrivées de type 1 par jour : ");
        System.out.println(cc.arrivalsCollector1.report());
        System.out.println(">> Rapport du nombre d'arrivées de type 2 par jour : ");
        System.out.println(cc.arrivalsCollector2.report());

        System.out.println(">> Rapport du nombre d'abandons de type 1 par jour : ");
        System.out.println(cc.abandonsCollector1.report());
        System.out.println(">> Rapport du nombre d'abandons de type 2 par jour : ");
        System.out.println(cc.abandonsCollector2.report());

        System.out
                .println(">> Rapport du nombre d'appels de type 1 répondus en moins de " + s + " secondes par jour : ");
        System.out.println(cc.goodWaitingTimesCollector1.report());
        System.out
                .println(">> Rapport du nombre d'appels de type 2 répondus en moins de " + s + " secondes par jour : ");
        System.out.println(cc.goodWaitingTimesCollector2.report());

        //System.out.println(Arrays.toString(cc.abandonsCollector1.getArray()));

//        System.out.println(Arrays.toString(cc.getBusynessAverageByAgentType(1)));
//        System.out.println(Arrays.toString(cc.getBusynessAverageByAgentType(2)));
        /*
        int day = 200;
        System.out.println("> Jour " + day + " :");
        System.out.println(">> nb abandons de type 1 : " + cc.getNbAbandonsByCallTypeAndByDay(1, day));
        System.out.println(">> nb abandons de type 2 : " + cc.getNbAbandonsByCallTypeAndByDay(2, day));
        System.out.println(">> nb appels de temps d'attente < 20s de type 1 : "
                + cc.getNbGoodWaitingTimesByCallTypeAndByDay(1, day));
        System.out.println(">> nb appels de temps d'attente < 20s de type 2 : "
                + cc.getNbGoodWaitingTimesByCallTypeAndByDay(2, day));
         */
        //print histograms
        cc.printHistogram(1);
        cc.printHistogram(2);

    }

}
