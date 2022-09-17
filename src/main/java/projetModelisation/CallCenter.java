package projetModelisation;

import java.util.ArrayList;
import java.util.LinkedList;
import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.stat.Tally;

public class CallCenter {

    public CallCenter(
            double lambda1, double lambda2,
            double mu11, double mu12, double mu21, double mu22,
            double nu1, double nu2,
            int n1, int n2,
            int s,
            int n,
            int T
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
        this.nbDays = n;
        this.nbHoursPerDay = T;
    }

    RandomVariateGen genArrivalTime1, genArrivalTime2;

    RandomVariateGen genServiceTimeC1A1, genServiceTimeC1A2,
            genServiceTimeC2A1, genServiceTimeC2A2;

    RandomVariateGen genPatienceTime1, genPatienceTime2;

    LinkedList<Call> listWaitingCalls1 = new LinkedList<Call>();
    LinkedList<Call> listWaitingCalls2 = new LinkedList<Call>();

    int nAgents1, nAgents2;
    LinkedList<Agent> listFreeAgents1 = new LinkedList<Agent>();
    LinkedList<Agent> listFreeAgents2 = new LinkedList<Agent>();
    LinkedList<Agent> listBusyAgents1 = new LinkedList<Agent>();
    LinkedList<Agent> listBusyAgents2 = new LinkedList<Agent>();

    int nAbandons1, nAbandons2;
    Tally abandonsCollector1 = new Tally("gets the nAbandons1 after each day");
    Tally abandonsCollector2 = new Tally("gets the nAbandons2 after each day");

    int goodWaitingTimesThreshold, nGoodWaitingTimes1, nGoodWaitingTimes2;
    Tally goodWaitingTimesCollector1 = new Tally("gets the nGoodWaitingTimes1 after each day");
    Tally goodWaitingTimesCollector2 = new Tally("gets the nGoodWaitingTimes2 after each day");

    int nbDays; // number of days
    int nbHoursPerDay; // number of hours per day (T)

    private class Agent {

        int agenType;
        Double listOccupationRates[] = new Double[nbDays];
        ArrayList<Call> CallResponded1, CallResponded2;

        public Agent() {
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
                new Arrival(type).schedule(genArrivalTime1.nextDouble());

                Call call = new Call();
                call.callType = this.type;
                call.arrivalTime = sim.time();
                call.patienceTime = genPatienceTime1.nextDouble();

                new Abandon(call).schedule(call.patienceTime);

                switch (getAgent(call.callType)) {
                    case 1 -> {
                        // an agent 1 took the call
                        Agent agent1 = (Agent) listFreeAgents1.removeFirst();
                        listBusyAgents1.add(agent1);
                        agent1.CallResponded1.add(call);
                        call.agentWhoResponded = agent1;
                        call.serviceTime = genServiceTimeC1A1.nextDouble();
                        // schedule the end of the call at  call.serviceTime (pass it the call id??)
                        nGoodWaitingTimes1++;
                    }
                    case 2 -> {
                        // an agent 2 took the call
                        Agent agent2 = (Agent) listFreeAgents2.removeFirst();
                        listBusyAgents2.add(agent2);
                        agent2.CallResponded1.add(call);
                        call.agentWhoResponded = agent2;
                        call.serviceTime = genServiceTimeC1A2.nextDouble();
                        // schedule the end of the call at  call.serviceTime
                        nGoodWaitingTimes1++;
                    }
                    default -> // no agent can take the call
                        listWaitingCalls1.addLast(call);
                }
            } else if (this.type == 2) {
                new Arrival(type).schedule(genArrivalTime2.nextDouble());

                Call call = new Call();
                call.callType = this.type;
                call.arrivalTime = sim.time();
                call.patienceTime = genPatienceTime2.nextDouble();

                new Abandon(call).schedule(call.patienceTime);

                switch (getAgent(call.callType)) {
                    case 1 -> {
                        // an agent 1 took the call
                        Agent agent1 = (Agent) listFreeAgents1.removeFirst();
                        listBusyAgents1.add(agent1);
                        agent1.CallResponded2.add(call);
                        call.agentWhoResponded = agent1;
                        call.serviceTime = genServiceTimeC2A1.nextDouble();
                        // schedule the end of the call at  call.serviceTime (pass it the call id??)
                        nGoodWaitingTimes2++;
                    }
                    case 2 -> {
                        // an agent 2 took the call
                        Agent agent2 = (Agent) listFreeAgents2.removeFirst();
                        listBusyAgents2.add(agent2);
                        agent2.CallResponded2.add(call);
                        call.agentWhoResponded = agent2;
                        call.serviceTime = genServiceTimeC2A2.nextDouble();
                        // schedule the end of the call at  call.serviceTime
                        nGoodWaitingTimes2++;
                    }
                    default -> // no agent can take the call
                        listWaitingCalls2.addLast(call);
                }
            }
        }

    }

    private int getAgent(int callType) {
        //to implement by Mrs MBAYE
        return callType;
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

    private int getAnotherCall(int agentType) {
        // to implement later...
        return 0;
    }

    class EndOfCall extends Event {

        Call call;

        public EndOfCall(Call call) {
            this.call = call;
        }

        @Override
        public void actions() {
            Agent agent = call.agentWhoResponded;
            agent.listOccupationRates[0] += call.serviceTime; // on est en train de gérer le 1er jour
            // we can maybe remove the call

            switch (agent.agenType) {
                case 1 -> {
                    listBusyAgents1.remove(agent);
                    listFreeAgents1.addLast(agent);
                    
                    if(listWaitingCalls1.size() > 0){
                        // cela veut dire que tous les agents 1 sont occupes
                        // donc il prend la tete de file
                        // on genere le serviceTime
                        // on programme la fin du call dans call.serviceTime
                        // on verifie si le temps d'attente est < s pour le garder (1)
                    }else if(listWaitingCalls2.size() > 0){
                        // cela veut dire que tous les agents 2 sont occupes
                        // donc il prend la tete de file
                        // on genere le serviceTime
                        // on programme la fin du call dans call.serviceTime
                        // on verifie si le temps d'attente est < s pour le garder (2)
                    } else {
                        // cela veut dire qu'aucun appel n'est en attente
                        // on le remet a la queue des agents 1 libres.
                    }
                    break;
                }
                case 2 -> {
                    listBusyAgents2.remove(agent);
                    listFreeAgents2.addLast(agent);
                    break;
                }

            }

        }

    }

    public static void main(String args[]) {
        // TODO code application logic here
    }
}
