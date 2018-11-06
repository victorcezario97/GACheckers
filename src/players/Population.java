package players;

import java.util.Random;

public class Population {
	
		public static final int N = 10;
		private static final int BOUND = 100;

		public static GAPlayer[] newPop() {
			GAPlayer[] pop = new GAPlayer[N];
			
			for(int i=0; i<N; i++) {
				pop[i] = newGAPlayer();
			}
			
			return pop;
		}
		
		public static GAPlayer[] crossOver(GAPlayer[] pop, int best) {
			Random rand = new Random();
			
			for(int i=0; i<N; i++) {
				
				if(i != best) {
					for(int j=0; j<5; j++) {
						pop[i].points[j] = ((pop[i].points[j] + pop[best].points[j]) / 2) + (-1+rand.nextInt(3)*rand.nextInt(10));
					}
				}
			}
			
			return pop;
		}
		
		public static void printPop(GAPlayer[] pop) {
			for(int i=0; i<N; i++) {
				System.out.printf("INDIVIDUAL %d:\n", i);
				System.out.printf("FITNESS: %d\n", pop[i].fitness);
				System.out.printf("POINTS: [%f] [%f] [%f] [%f] [%f]\n\n", pop[i].points[0], pop[i].points[1], pop[i].points[2], pop[i].points[3], pop[i].points[4]);
			}
		}
		
		public static GAPlayer newGAPlayer() {
			Random rand = new Random();
			
			return new GAPlayer(rand.nextInt(BOUND), rand.nextInt(BOUND), rand.nextInt(BOUND), rand.nextInt(BOUND), rand.nextInt(BOUND));
		}
		
		
}
