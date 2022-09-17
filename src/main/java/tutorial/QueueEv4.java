package tutorial;

import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;
// La file M/M/s+M

public class QueueEv4 {

    RandomVariateGen genArr;
    RandomVariateGen genServ;
    RandomVariateGen genPatience;
    LinkedList<Customer> waitList = new LinkedList<Customer>();
    LinkedList<Customer> servList = new LinkedList<Customer>();
    Tally custWaits = new Tally("Waiting times");
    Accumulate totWait = new Accumulate("Size of queue");
    int abandon = 0;
    int serveurs;

    class Customer {

        double arrivTime, servTime, patienceTime;
    }

    public QueueEv4(double lambda, double mu, double nu, int serveurs) {
        genArr = new ExponentialGen(new MRG32k3a(), lambda);
        genServ = new ExponentialGen(new MRG32k3a(), mu);
        genPatience = new ExponentialGen(new MRG32k3a(), nu);
        this.serveurs = serveurs;
    }

    public void simulate(double timeHorizon) {
        Sim.init();
        new EndOfSim().schedule(timeHorizon);
        new Arrival().schedule(genArr.nextDouble());
        Sim.start();
    }

    class Arrival extends Event {

        public void actions() {
            new Arrival().schedule(genArr.nextDouble()); // Next arrival.
            Customer cust = new Customer();  // Cust just arrived.
            cust.arrivTime = Sim.time();
            cust.servTime = genServ.nextDouble();
            cust.patienceTime = genPatience.nextDouble();
            if (servList.size() >= serveurs) {       // Must join the queue.
                waitList.addLast(cust);
                new Abandon(cust).schedule(cust.patienceTime);
                totWait.update(waitList.size());
            } else {                         // Starts service.
                custWaits.add(0.0);
                servList.addLast(cust);
                new Departure().schedule(cust.servTime);
            }
        }
    }

    class Abandon extends Event {

        Customer cust;

        public Abandon(Customer cust) {
            this.cust = cust;
        }

        public void actions() {
            if (waitList.contains(cust)) {
                waitList.remove(cust);
                totWait.update(waitList.size());
                abandon++;
            }
        }
    }

    class Departure extends Event {

        public void actions() {
            servList.removeFirst();
            if (waitList.size() > 0) {
                // Starts service for next one in queue.
                Customer cust = waitList.removeFirst();
                totWait.update(waitList.size());
                custWaits.add(Sim.time() - cust.arrivTime);
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

        double mu = 2.5;
        double lambda = 5.9;
        int serveurs = 3;
        double nu = 0.1;
        QueueEv4 queue = new QueueEv4(lambda, mu, nu, serveurs);
        queue.simulate(1000.0);
        System.out.println(queue.custWaits.report());
        System.out.println(queue.totWait.report());
        System.out.println(queue.abandon);

    }
}
