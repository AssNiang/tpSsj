package projetModelisation;

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
        this.genArrivalTime2 = new ExponentialGen(new MRG32k3a(), lambda2);;
        this.genServiceTimeC1A1 = new ExponentialGen(new MRG32k3a(), mu11);;
        this.genServiceTimeC1A2 = new ExponentialGen(new MRG32k3a(), mu12);;
        this.genServiceTimeC2A1 = new ExponentialGen(new MRG32k3a(), mu21);;
        this.genServiceTimeC2A2 = new ExponentialGen(new MRG32k3a(), mu22);;
        this.genPatienceTime1 = new ExponentialGen(new MRG32k3a(), nu1);;
        this.genPatienceTime2 = new ExponentialGen(new MRG32k3a(), nu2);;
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

    LinkedList<Call> waitingCalls1 = new LinkedList<Call>();
    LinkedList<Call> waitingCalls2 = new LinkedList<Call>();

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
        int nbCallResponded1, nbCallResponded2;

        public Agent() {
        }
    }

    private class Call {

        int callType;
        double arrivalTime;
        double serviceTime;
        double patienceTime;
        int agentWhoRespondedType;

        public Call() {
        }
    }

    private class Arrival extends Event {

        @Override
        public void actions() {
            //...
        }

    }

    public static void main(String args[]) {
        // TODO code application logic here
    }
}
