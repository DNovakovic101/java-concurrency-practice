package trinaestiprvi;
import java.util.concurrent.atomic.AtomicInteger;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;


/*
 * U frizerskom salonu rade dva berberina. Ako nema musterija, berber sedi u
 * svojoj stolici i spava. Kada musterija udje, ako neki od berbera spava, budi
 * ga, seda za stolicu i berber je sisa. Ako su svi berberi zauzeti, musterija
 * seda za stolicu u cekaonici i ceka da se oslobodi neko od berbera. Kada
 * berber zavrsi sisanje musterije, ako ima musterija koje cekaju, krece da
 * sisa jednu od musterija koje cekaju. Ako nema vise musterija koje cekaju,
 * berber seda u svoju stolicu i spava.
 * 
 * Implementirati sinhronizaciju ove dve vrste procesa kako je opisano.
 */
public class BerberiAtomici  extends Application{

	private class Pristup {
		
		private AtomicInteger brBerbera = new AtomicInteger(0);
		private AtomicInteger brMusterija = new AtomicInteger(0);

		private void sacekajMusteriju() throws InterruptedException {
			brBerbera.incrementAndGet();
			
			boolean ok;
			do {
				int oldV = brMusterija.get();
				int newV = oldV - 1;
				ok = newV >= 0;
				if(ok) { ok = brMusterija.compareAndSet(oldV, newV);}
				else { Thread.yield(); }
			}while(!ok);
		}

		private void sacekajBerbera() throws InterruptedException {
			brMusterija.incrementAndGet();
			
			boolean ok;
			do {
				int oldV = brBerbera.get();
				int newV = oldV - 1;
				ok = newV >= 0;
				if(ok) { ok = brBerbera.compareAndSet(oldV, newV);}
				else { Thread.yield(); }
			}while(!ok);
		}
	}

	private Pristup pristup = new Pristup();

	@AutoCreate(2)
	protected class Berber extends Thread {

		@Override
		protected void step() {
			try {
				pristup.sacekajMusteriju();
				sisa();
			} catch (InterruptedException e) {
				stopGracefully();
			}
		}
	}

	@AutoCreate
	protected class Musterija extends Thread {

		@Override
		protected void run() {
			try {
				pristup.sacekajBerbera();
				sisaSe();
			} catch (InterruptedException e) {
				stopGracefully();
			}
		}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container cekaonica = box("??????????????????");
	protected final Container stolice   = box("??????????");
	protected final Container main      = column(cekaonica, stolice);
	protected final Operation berber    = init().name("???????????? %d").color(ROSE).text("??????????").container(stolice).update(this::azuriraj);
	protected final Operation musterija = duration("1??1").name("????????. %d").color(AZURE).text("????????").container(cekaonica).update(this::azuriraj);
	protected final Operation sisanjeB  = duration("7").text("????????").update(this::azuriraj);
	protected final Operation sisanjeM  = duration("7").text("???????? ????").container(stolice).colorAfter(CHARTREUSE).textAfter("???????????? ????").update(this::azuriraj);

	protected void sisa() {
		sisanjeB.performUninterruptibly();
	}

	protected void sisaSe() {
		sisanjeM.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		long brB1 = 0;
		long brB2 = 0;
		for (Berber t : stolice.getItems(Berber.class)) {
			if (sisanjeB.getTextBefore().equals(t.getText())) {
				brB1++;
			} else {
				brB2++;
			}
		}
		long brM1 = stolice.stream(Musterija.class).count();
		long brM2 = cekaonica.stream(Musterija.class).count();
		cekaonica.setText(String.format("%d", brM2));
		stolice.setText(String.format("%d : %d", brB1, brM1));
		long razlika = brB1 - brM1;
		if (brB2 > 0 && brM2 > 0) {
			cekaonica.setColor(MAROON);
		} else {
			cekaonica.setColor(OLIVE);
		}
		if (razlika == 0) {
			stolice.setColor(ARMY);
		} else {
			stolice.setColor(MAROON);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] arguments) {
		launch("???????????????? ??????????????");
	}
}
