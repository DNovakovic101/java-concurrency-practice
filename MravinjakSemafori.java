package trinaestiprvi;

import java.util.concurrent.Semaphore;
 
/*
 * Napisati program koji kreira jedan novi mravinjak i 20 mrava predstavljenih
 * zasebnim pozadinskim procesima. Prvih 15 mrava su radnici, dok su preostalih
 * 5 zidari, i zovu se "Mrav 1", "Mrav 2"... "Mrav 20". Program potom ceka 30
 * sekundi posle cega stampa velicinu mravinjaka i zavrsava rad. (5 poena)
 * 
 * Mravi radnici ceo dan skupljaju hranu (pomocu metoda Priroda::nadjiHranu) i
 * donose je u mravinjak. (5 poena)
 * 
 * Mravi zidari dan provode u mravinjaku i stalno ga prosiruju (pomocu metoda
 * Priroda::prosiriMravinjak). Prosirivanje mravinjaka zahteva puno energije i
 * zidari pre ovoga moraju da pojedu 25 grama hrane. (5 poena)
 * 
 * Sinhronizovati klasu Mravinjak tako da se ni u kom slučaju ne izgubi hrana.
 * Takodje, blokirati mrava koji pokusa da jede hranu ako je nema dovoljno ili
 * da doda hranu ako je mravinjak pun. U mravinjak velicine 1 moze stati 50
 * grama hrane, u mravinjak velicine 2 moze stati 60 grama, u mravinjak velicine
 * 3 moze stati 70 grama, itd. Odblokirati mrave cim se stvore uslovi za
 * nastavak rada. (10 poena)
 * 
 * Obratiti paznju na elegantnost i objektnu orijentisanost realizacije i stil
 * resenja. Za program koji se ne kompajlira, automatski se dobija 0 poena bez
 * daljeg pregledanja.
 */
public class MravinjakSemafori {
    public static void main(String[] args) throws InterruptedException {
        Mravinjak mravinjak = new Mravinjak();
        Priroda priroda = new Priroda();
 
        for (int i = 0; i < 15; i++) {
            MravRadnik radnik = new MravRadnik(mravinjak, priroda);
            radnik.setName("Mrav " + i);
            radnik.start();
 
            if (i > 9) {
                int j = i + 6;
                Thread helpMrav = new Thread(new MravZidar(mravinjak, priroda));
                helpMrav.setDaemon(true);
                helpMrav.setName("Mrav " + j);
                helpMrav.start();
            }
        }
 
        Thread.sleep(30000);
        System.out.println("Velicina mravinjaka je\t---> " + mravinjak.getVelicina());
    }
}
 
class MravRadnik extends Thread {
    private final Mravinjak mravinjak;
    private final Priroda priroda;
 
    public MravRadnik(Mravinjak mravinjak, Priroda priroda) {
        this.mravinjak = mravinjak;
        this.priroda = priroda;
        setDaemon(true);
    }
 
    @Override
    public void run() {
        while (!interrupted()) {
            int kolicinaNadjeneHrane = priroda.nadjiHranu();
 
            mravinjak.donesiHranu(kolicinaNadjeneHrane);
 
        }
    }
 
}
 
class MravZidar implements Runnable {
    private final Mravinjak mravinjak;
    private final Priroda priroda;
 
    public MravZidar(Mravinjak mravinjak, Priroda priroda) {
        this.mravinjak = mravinjak;
        this.priroda = priroda;
    }
 
    @Override
    public void run() {
        while (!Thread.interrupted()) {
 
            mravinjak.pojediHranu(25);
 
            priroda.prosiriMravinjak();
            mravinjak.uvecaj();
        }
    }
 
}
 
/*
 * Sinhronizovati klasu Mravinjak tako da se ni u kom slučaju ne izgubi hrana.
 * 
 * 
 * Takodje, blokirati mrava koji pokusa da jede hranu ako je nema dovoljno ili
 * da doda hranu ako je mravinjak pun. 
 * 
 * U mravinjak velicine 1 moze stati 50
 * grama hrane, u mravinjak velicine 2 moze stati 60 grama, u mravinjak velicine
 * 3 moze stati 70 grama, itd. Odblokirati mrave cim se stvore uslovi za
 * nastavak rada. (10 poena)
 */
 
class Mravinjak {
 
	private Semaphore mutex = new Semaphore(1);
	private Semaphore kolicinaHrane = new Semaphore(0);
	private Semaphore velicinaSkladista = new Semaphore(50);
 
    private int hrana = 0;
    private int velicina = 1;
 
    public int getVelicina() {
        return velicina;
    }
 
    public void donesiHranu(int koliko)  {
    	velicinaSkladista.acquireUninterruptibly(koliko);
        
    	mutex.acquireUninterruptibly();
    	try {
            hrana += koliko;
    	}catch(Exception e) {
    		velicinaSkladista.release();
    		throw e;
    	}finally {
    		mutex.release();
    	}
    	
    	kolicinaHrane.release(koliko);
    }
 
    public void pojediHranu(int koliko) {
        
    	kolicinaHrane.acquireUninterruptibly(koliko);
    	
    	mutex.acquireUninterruptibly();
    	try {
    		 hrana -= koliko;
    	}catch(Exception e) {
    		kolicinaHrane.release();
    		throw e;
    	}finally {
    		mutex.release();
    	}
            
    	velicinaSkladista.release(koliko);
    }
 
    public void uvecaj() {
    	
    	mutex.acquireUninterruptibly();
    	try {
    		velicina++;
    		velicinaSkladista.release(10);
    	}finally {
    		mutex.release();
    	}

 
    }
}
 
class Priroda {
 
    public static int nadjiHranu() {
        int n = (int) (2 + 8 * Math.random());
        try {
            Thread.sleep((long) (1000 + 1000 * Math.random()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(Thread.currentThread().getName() + " je pronasao " + n + " grama hrane.");
        return n;
    }
 
    public static void prosiriMravinjak() {
        try {
            Thread.sleep((long) (1000 + 3000 * Math.random()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(Thread.currentThread().getName() + " je prosirio mravinjak.");
    }
}