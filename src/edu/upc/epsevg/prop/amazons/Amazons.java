package edu.upc.epsevg.prop.amazons;

import edu.upc.epsevg.prop.amazons.players.FinalChicken;
import edu.upc.epsevg.prop.amazons.players.CarlinhosPlayer;
import edu.upc.epsevg.prop.amazons.players.RandomPlayer;
import javax.swing.SwingUtilities;

/**
 *
 * @author bernat
 */
public class Amazons {
        /**
     * @param args
     */
    public static void main(String[] args) {
        
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                IPlayer player1 = new FinalChicken();
                IPlayer player2 = new CarlinhosPlayer();
                
                // FULL_BOARD...
                new AmazonsBoard(player1 , player2, 2, Level.FULL_BOARD);
                
            }
        });
    }
}
