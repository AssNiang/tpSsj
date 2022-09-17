package tutorial;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;
//La file M/M/1/K
public class QueueEv2 {

   RandomVariateGen genArr;
   RandomVariateGen genServ;
   LinkedList<Customer> waitList = new LinkedList<Customer> ();
   LinkedList<Customer> servList = new LinkedList<Customer> ();
   Tally custWaits     = new Tally ("Waiting times");
   Accumulate totWait  = new Accumulate ("Size of queue");
    int K;
   class Customer { double arrivTime, servTime; }

   public QueueEv2 (double lambda, double mu, int K) {
      genArr = new ExponentialGen (new MRG32k3a(), lambda);
      genServ = new ExponentialGen (new MRG32k3a(), mu);
      this.K=K;
      
   }

   public void simulate (double timeHorizon) {
      Sim.init();
      new EndOfSim().schedule (timeHorizon);
      new Arrival().schedule (genArr.nextDouble());
      Sim.start();
   }

   class Arrival extends Event {
      public void actions() {
         new Arrival().schedule (genArr.nextDouble()); // Next arrival.
         Customer cust = new Customer();  // Cust just arrived.
         cust.arrivTime = Sim.time();
         cust.servTime = genServ.nextDouble();
         if (servList.size() >0) {       // Must join the queue.
           if(waitList.size()<K)
            { waitList.addLast (cust);
             totWait.update (waitList.size());}
             
         } else {                         // Starts service.
        	custWaits.add (0.0);
            servList.addLast (cust);
            new Departure().schedule (cust.servTime);
         }
      }
   }

   class Departure extends Event {
      public void actions() {
         servList.removeFirst();
         if (waitList.size() > 0) {
            // Starts service for next one in queue.
            Customer cust = waitList.removeFirst();
            totWait.update (waitList.size());
             custWaits.add (Sim.time() - cust.arrivTime);
            servList.addLast (cust);
            new Departure().schedule (cust.servTime);
         }
      }
   }

   class EndOfSim extends Event {
      public void actions() {
         Sim.stop();
      }
   }
  
   
   double puissance(double a, int n){
		 if(n==0)
			 return 1;
		 else
		 { double v=1;
		   for(int i=1;i<=n;i++)
			  v=v*a;
		   return v;
		   } 
	 }
   
   double P_0(double rho, int K){
	 return (1-rho)/(1- puissance(rho,K+1))  ;
   }
   
   double getL(double rho, int K){
	return   rho/(1-rho)- ((K+1)*puissance(rho,K+1))/(1- puissance(rho,K+1));
   }
   
   double getL_q(double rho, int K) {
	   double p_0=P_0(rho,K);
	   double L=getL(rho,K);
	   return L-(1-p_0);
   }
   
  double getW_q(double rho, int K, double lambda){
	  double L_q= getL_q(rho,K);
	  return L_q/lambda;
  }
   
   public static void main (String[] args) {
	
	  double mu=2.0;
	  double lambda= 1.0;
	  double rho=lambda/mu;
	  int K=20;
      QueueEv2 queue = new QueueEv2 (lambda, mu,K);
      queue.simulate (10000000.0);
      System.out.println (queue.custWaits.report());
      System.out.println (queue.totWait.report());
       
      double Wq=queue.getW_q(rho, K, lambda);
      System.out.println ("W_q="+Wq);
      double Lq=queue.getL_q(rho, K);
      System.out.println ("L_q="+Lq);
   }
}
