package tutorial;

import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;
//Le modele V

public class QueueEv3 {

    RandomVariateGen genArr1, genArr2;
    RandomVariateGen genServ1, genServ2;
    LinkedList<Customer> waitList1 = new LinkedList<Customer>();
    LinkedList<Customer> waitList2 = new LinkedList<Customer>();
    LinkedList<Customer> servList = new LinkedList<Customer>();
    Tally custWaits1 = new Tally("Waiting times 1");
    Tally custWaits2 = new Tally("Waiting times 2");
    Accumulate totWait1 = new Accumulate("Size of queue 1");
    Accumulate totWait2 = new Accumulate("Size of queue 2");
    int serveurs;
    RandomStream genU = new MRG32k3a();

    class Customer {

        double arrivTime, servTime;
        int type;
    }

    public QueueEv3(double lambda1, double lambda2, double mu1, double mu2, int serveurs) {
        genArr1 = new ExponentialGen(new MRG32k3a(), lambda1);
        genServ1 = new ExponentialGen(new MRG32k3a(), mu1);

        genArr2 = new ExponentialGen(new MRG32k3a(), lambda2);
        genServ2 = new ExponentialGen(new MRG32k3a(), mu2);

        this.serveurs = serveurs;
    }

    public void simulate(double timeHorizon) {
        Sim.init();
        new EndOfSim().schedule(timeHorizon);
        new Arrival(1).schedule(genArr1.nextDouble());
        new Arrival(2).schedule(genArr2.nextDouble());
        Sim.start();
    }

    class Arrival extends Event {

        int type;

        public Arrival(int type) {
            this.type = type;
        }

        public void actions() {
            if (type == 1) {
                new Arrival(1).schedule(genArr1.nextDouble()); // Next arrival.
                Customer cust = new Customer();  // Cust just arrived.
                cust.arrivTime = Sim.time();
                cust.servTime = genServ1.nextDouble();
                cust.type = 1;
                if (servList.size() >= serveurs) {       // Must join the queue.
                    waitList1.addLast(cust);
                    totWait1.update(waitList1.size());

                } else {                         // Starts service.
                    custWaits1.add(0.0);
                    servList.addLast(cust);
                    new Departure().schedule(cust.servTime);
                }
            } else {
                new Arrival(2).schedule(genArr2.nextDouble()); // Next arrival.
                Customer cust = new Customer();  // Cust just arrived.
                cust.arrivTime = Sim.time();
                cust.servTime = genServ2.nextDouble();
                cust.type = 2;
                if (servList.size() >= serveurs) {       // Must join the queue.
                    waitList2.addLast(cust);
                    totWait2.update(waitList2.size());

                } else {                         // Starts service.
                    custWaits2.add(0.0);
                    servList.addLast(cust);
                    new Departure().schedule(cust.servTime);
                }

            }
        }

    }

    public int getCustomer() {
        if (waitList1.size() == 0 && waitList2.size() == 0) {
            return 0;
        } else if (waitList1.size() != 0) {
            return 1;
        } else {
            return 2;
        }
    }

    public int getCustomer2() {
        if (waitList1.size() == 0 && waitList2.size() == 0) {
            return 0;
        } else if (waitList1.size() > waitList2.size()) {
            return 1;
        } else if (waitList2.size() > waitList1.size()) {
            return 2;
        } else {
            if (genU.nextDouble() < 0.4) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    class Departure extends Event {

        public void actions() {
            servList.removeFirst();

            if (getCustomer2() == 1) {
                // Starts service for next one in queue.
                Customer cust = waitList1.removeFirst();
                totWait1.update(waitList1.size());
                custWaits1.add(Sim.time() - cust.arrivTime);
                servList.addLast(cust);
                new Departure().schedule(cust.servTime);
            }
            if (getCustomer2() == 2) {
                // Starts service for next one in queue.
                Customer cust = waitList2.removeFirst();
                totWait2.update(waitList2.size());
                custWaits2.add(Sim.time() - cust.arrivTime);
                servList.addLast(cust);
                new Departure().schedule(cust.servTime);
            }

        }
    }

    class EndOfSim extends Event {

        public void actions() {
            Sim.stop();
        }
    }

    public static void main(String[] args) {

        double mu1 = 2.0;
        double lambda1 = 1.0;

        double mu2 = 2.2;
        double lambda2 = 2.1;

        int serveurs = 3;
        QueueEv3 queue = new QueueEv3(lambda1, lambda1, mu1, mu2, serveurs);
        queue.simulate(1000.0);
        System.out.println(queue.custWaits1.report());
        System.out.println(queue.totWait1.report());
        System.out.println("=============================");
        System.out.println(queue.custWaits2.report());
        System.out.println(queue.totWait2.report());

    }
}
