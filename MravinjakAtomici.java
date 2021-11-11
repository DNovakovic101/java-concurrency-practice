package trinaestiprvi;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
 
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
public class MravinjakAtomici {
    public static void main(String[] args) throws InterruptedException {
        Mravinjak2 mravinjak = new Mravinjak2();
        Priroda2 priroda = new Priroda2();
 
        for (int i = 0; i < 15; i++) {
            MravRadnik2 radnik = new MravRadnik2(mravinjak, priroda);
            radnik.setName("Mrav " + i);
            radnik.start();
 
            if (i > 9) {
                int j = i + 6;
                Thread helpMrav = new Thread(new MravZidar2(mravinjak, priroda));
                helpMrav.setDaemon(true);
                helpMrav.setName("Mrav " + j);
                helpMrav.start();
            }
        }
 
        Thread.sleep(30000);
        System.out.println("Velicina mravinjaka je\t---> " + mravinjak.getVelicina());
    }
}
 
class MravRadnik2 extends Thread {
    private final Mravinjak2 mravinjak;
    private final Priroda2 priroda;
 
    public MravRadnik2(Mravinjak2 mravinjak, Priroda2 priroda) {
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
 
class MravZidar2 implements Runnable {
    private final Mravinjak2 mravinjak;
    private final Priroda2 priroda;
 
    public MravZidar2(Mravinjak2 mravinjak, Priroda2 priroda) {
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
 
class Mravinjak2 {
 
	private AtomicBoolean mutex = new AtomicBoolean(true);
	private AtomicInteger kolicinaHrane = new AtomicInteger(0);
	private AtomicInteger velicinaSkladista = new AtomicInteger(50);
	
    private int hrana = 0;
    private int velicina = 1;
 
    public int getVelicina() {
        return velicina;
    }
 
    public void donesiHranu(int koliko)  {
        boolean ok;
        do {
        	int oldV = velicinaSkladista.get();
        	int newV = oldV - koliko ;
        	ok = newV >= 0 ;
        	if(ok) { ok = velicinaSkladista.compareAndSet(oldV, newV);}
        	else { Thread.yield(); }
        }while(!ok);
        
    	while(!mutex.compareAndSet(true, false)) { Thread.yield(); }
           try { 
        	   hrana += koliko;
           }catch(Exception e){
        	   throw e;
           }finally {
        	   mutex.set(true);
           }
           kolicinaHrane.addAndGet(koliko);
    }
 
    public void pojediHranu(int koliko) {
        boolean ok;
        do {
        	int oldV = kolicinaHrane.get();
        	int newV = oldV - koliko ;
        	ok = newV >= 0 ;
        	if(ok) { ok = kolicinaHrane.compareAndSet(oldV, newV);}
        	else { Thread.yield(); }
        }while(!ok);
        
    	
    	while(!mutex.compareAndSet(true, false)) { Thread.yield(); }
        try { 
        	hrana -= koliko;
        }catch(Exception e){
        	throw e;
        }finally {
     	   mutex.set(true);
        }
        velicinaSkladista.addAndGet(koliko);

    }
 
    public void uvecaj() {
    	while(!mutex.compareAndSet(true, false)) { Thread.yield(); }
        try { 
        	   velicina++;
        }finally {
     	   mutex.set(true);
        }

 
    }
}
 
class Priroda2 {
 
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