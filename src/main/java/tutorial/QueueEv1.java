package tutorial;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;
//M/M/s
public class QueueEv1 {

   RandomVariateGen genArr;
   RandomVariateGen genServ;
   LinkedList<Customer> waitList = new LinkedList<Customer> ();
   LinkedList<Customer> servList = new LinkedList<Customer> ();
   Tally custWaits     = new Tally ("Waiting times");
   Accumulate totWait  = new Accumulate ("Size of queue");
   int s;
   
   class Customer { double arrivTime, servTime; }

   public QueueEv1 (double lambda, double mu, int s) {
      genArr = new ExponentialGen (new MRG32k3a(), lambda);
      genServ = new ExponentialGen (new MRG32k3a(), mu);
      this.s=s;
      
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
         if (servList.size() >=s) {       // Must join the queue.
            waitList.addLast (cust);
            totWait.update (waitList.size());
         } else {                         // Starts service.
            if(Sim.time()>10000)
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
           if(Sim.time()>10000)
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
   
   
 /** Compute performance mesure******
  * 
  * */
  double P_0(double rho, int C){
		double som=0;
		for(int n=0;n<=C-1;n++)
		  som+=puissance(rho,n)/fact(n);
		
		return 1/(som+ (puissance(rho,C)/(fact(C-1)*(C-rho))));
	 }  
 double getL_q(double rho, int C, double P_0){
	 
       double n=1;
       for(int i=1; i<=C+1;i++)
    	     n=n*rho;
       double  d1=1;
       for(int i=1;i<=C-1;i++)
    	     d1=d1*i;
       double d=d1*(C-rho)*(C-rho);
       
	 return (n/d)*P_0;	 
 }
 
 int fact(int n)
 {  
    if(n==0)
    	  return 1;
    else 
    { int v=1;
    	  for(int i=1;i<=n;i++) 
	   v=v*i;
	  return v;
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
 
   public static void main (String[] args) {
	
	  double mu=2.0;
	  double lambda= 5.0;
	  double rho=lambda/mu;
	  int C=3;
	 
      QueueEv1 queue = new QueueEv1 (lambda, mu,C);
      double p_0=queue.P_0(rho, C);
      double Lq=queue.getL_q(rho, C, p_0);
      double Wq=Lq/lambda;
      
      queue.simulate (1000000.0);
      System.out.println (queue.custWaits.report());
      System.out.println (queue.totWait.report());
        
      System.out.println ("Wq="+Wq);
      System.out.println ("Lq="+Lq);
     
   }
}
