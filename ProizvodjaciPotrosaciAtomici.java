package trinaestiprvi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Color;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * Dat je bafer fiksne velicine. Vise procesa zeli da istovremeno dodaje i
 * uklanja elemente sa ovog bafera.
 * 
 * Realizovati operaciju dodavanja tako da, ako je bafer pun, blokira proces
 * dok se ne oslobodi mesto za novi element. Takodje, realizovati operaciju
 * uklanjanja tako da, ako je bafer prazam, blokira proces dok se ne doda novi
 * element. 
 */
public class ProizvodjaciPotrosaciAtomici extends Application {
	protected Bafer bafer = new Bafer(12);

	protected class Bafer {

		private final List<Element> lista = new ArrayList<>();
		private final int velicina;

		private AtomicBoolean mutex = new AtomicBoolean(true);
		private AtomicInteger brMesta;
		private AtomicInteger brProizvoda = new AtomicInteger(0);

		public Bafer(int velicina) {
			this.velicina = velicina;
			this.brMesta = new AtomicInteger(velicina);
		}

		public void stavi(Element o) {
			boolean ok;
			do {
				int oldV = brMesta.get();
				int newV = oldV -1 ;
				ok = newV >= 0;
				if(ok) { ok = brMesta.compareAndSet(oldV, newV);}
				else { Thread.yield(); }
			}while(!ok);
			
			
			while (!mutex.compareAndSet(true, false)) {
				Thread.yield();
			}
			try {
				
				lista.add(o);
				elementi.addItem(o);
				
			} catch(Exception e){
				brMesta.incrementAndGet();
				throw e;
			}finally {
				mutex.set(true);
			}
			brProizvoda.incrementAndGet();
		}

		public Element uzmi() {
			boolean ok;
			do {
				int oldV = brProizvoda.get();
				int newV = oldV -1 ;
				ok = newV >= 0;
				if(ok) { ok = brProizvoda.compareAndSet(oldV, newV);}
				else { Thread.yield(); }
			}while(!ok);
			
			
			while (!mutex.compareAndSet(true, false)) {
				Thread.yield();
			}
			try {
				
				Element result = lista.remove(0);
				elementi.removeItem(result);
				
				brMesta.incrementAndGet();
				return result;
			} catch(Exception e){
				brProizvoda.incrementAndGet();
				throw e;
			}finally {
				mutex.set(true);
			}

		}
	}

	// ------------------- //
	// Sistemski deo //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	@AutoCreate(4)
	protected class Proizvodjac extends Thread {

		private final int id = getID();
		private int br = 0;

		@Override
		protected void step() {
			Element element = proizvedi(id + "x" + (br++));
			bafer.stavi(element);
		}

	}

	@AutoCreate(4)
	protected class Potrosac extends Thread {

		@Override
		protected void step() {
			Element element = bafer.uzmi();
			potrosi(element);
		}
	}

	protected final Container proizvodjaci = box("??????????????????????").color(NAVY);
	protected final Container potrosaci = box("??????????????????").color(MAROON);
	protected final Container elementi = box("????????????????").color(NEUTRAL_GRAY);
	protected final Container main = row(proizvodjaci, elementi, potrosaci);
	protected final Operation proizvodjac = init().name("????????????. %d").color(AZURE).text("????????").container(proizvodjaci);
	protected final Operation potrosac = init().name("????????. %d").color(ROSE).text("????????").container(potrosaci);
	protected final Operation element = init();
	protected final Operation proizvodnja = duration("3??1").text("??????????????????").textAfter("????????");
	protected final Operation potrosnja = duration("7??2").text("?????????? %s").textAfter("????????");

	protected Element proizvedi(String vrednost) {
		proizvodnja.performUninterruptibly();
		return new Element(vrednost);
	}

	protected void potrosi(Element element) {
		potrosnja.performUninterruptibly(element.getName());
	}

	protected class Element extends Item {

		public Element(String vrednost) {
			setName(vrednost);
		}

		private int getIndex() {
			return bafer.lista.indexOf(this);
		}

		@Override
		public Color getColor() {
			int index = getIndex();
			if ((index >= 0) && (index < bafer.velicina)) {
				return CHARTREUSE;
			} else {
				return ORANGE;
			}
		}

		@Override
		public String getText() {
			return String.format("Bafer[%d]", getIndex());
		}
	}

	public static void main(String[] arguments) {
		launch("?????????????????????? ?? ??????????????????");
	}
}
