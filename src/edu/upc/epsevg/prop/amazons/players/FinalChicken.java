/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upc.epsevg.prop.amazons.players;

import edu.upc.epsevg.prop.amazons.CellType;
import edu.upc.epsevg.prop.amazons.GameStatus;
import edu.upc.epsevg.prop.amazons.IAuto;
import edu.upc.epsevg.prop.amazons.IPlayer;
import edu.upc.epsevg.prop.amazons.Move;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;


import java.util.Random;

/**
 *
 * @author bernat
 */
public class FinalChicken implements IPlayer, IAuto {

    String name;
    // profunditat
    int prof;
    
    // Guardem la posicio de les diferents amazones en una llista per a cada color.
    LinkedList<Point> Blanques, Negres;
    
    public FinalChicken() {
        this.name = "FinalChicken";
        this.prof = 3;
        
        // Inicialitzem les peces amb la posicio que tindran en un estat inicial
        Blanques = new LinkedList<Point>();
        Blanques.add(new Point(1,3));
        Blanques.add(new Point(4,1));
        Blanques.add(new Point(7,1));
        Blanques.add(new Point(10,4));
        
        Negres = new LinkedList<Point>();
        Negres.add(new Point(1,7));
        Negres.add(new Point(4,10));
        Negres.add(new Point(7,10));
        Negres.add(new Point(10,7));
        
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        
        int Alpha = Integer.MIN_VALUE; 
        int Beta = Integer.MAX_VALUE;
        int profunditat = this.prof;
        
        boolean esBlanca = false;
        
        CellType color = s.getCurrentPlayer();
        if(color.name() == "PLAYER1")
            esBlanca = true;
         
        // Cal saber la posicio de las fitxes del enemic.
       
        obtenirPosicionsEnemigues(s, color);
        
        // Posarem el millor Moviment en una array on el primer element es el punt on es moura l'amazona i el segon on colocarem la fletxa.
        Point[] millorMoviment = new Point[2];
        Point[] moviment = new Point[2];
        
        // igual hay que cambiar el  4 luego !!!
        for(int i = 0; i<4; i++)
        {
            if(esBlanca){
                ArrayList<Point> MovimentsAmazona =  s.getAmazonMoves(Blanques.get(i), false); // restricted ???
                for(int m = 0; m<MovimentsAmazona.size(); m++)
                {
                    GameStatus statusCopia = s;
                    statusCopia.moveAmazon(Blanques.get(i), MovimentsAmazona.get(m));
                    
                    LinkedList<Point> casellesBuides = casellasDisponibles(statusCopia);
                    for(int c = 0; c < casellesBuides.size(); c++)
                    {
                        GameStatus statusCopiaFletxa = statusCopia;
                        statusCopiaFletxa.placeArrow(casellesBuides.get(c));
                        
                        // Posarem el millor moviment en una array 
                        moviment[0] = MovimentsAmazona.get(m);
                        moviment[1] = casellesBuides.get(c);
                        int eval = Minimitzador(statusCopiaFletxa, color.opposite(color), profunditat-1, Alpha, Beta, moviment);
                        if(eval > Alpha) {
                           millorMoviment = moviment;                              // guardarem la columna com a millorMoviment fins que un altre doni millor heuristica 
                           Alpha = eval;                                           // actualitzem Alfa amb la millor heurisitica que hem trobat de moment.
                       }
                    
                    }
                    
                    
                    
                    
                    
                    
                }
            }    
        }
        
        
        
       
    }
    
    
    /**
     * Funció que ens reitorna una llista amb totes les caselles buides del taulell
     */

    private LinkedList<Point> casellasDisponibles(GameStatus s){
    
        LinkedList<Point> casellesBuides = new LinkedList<Point>();
        for(int i = 0; i<s.getSize(); i++)
            for(int j = 0; j<s.getSize(); j++)
                if(s.getPos(i,j).name() == "EMPTY")
                    casellesBuides.add(new Point(i, j));
        return casellesBuides;
    }
    
    
    
    /**
     * Funció que ens actualiza el vector AmazonesEnemigues amb la posicio actual de cadascuna
     */
    private void obtenirPosicionsEnemigues(GameStatus s, CellType color){
        
        System.out.println("QUIEN SOY? "+color.opposite(color));
       
        
        if(color.name() == "PLAYER1") // esBlanca
        {
          for(int i = 0; i<Negres.size(); i++)
          {
              // Intrecambiem la posicio antiga per la posicio actual.
              Negres.set(i, s.getAmazon(color.opposite(color) , i));                            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          }
        }
        else{  // es Negra
            
            for(int i = 0; i<Blanques.size(); i++)
          {
              // Intrecambiem la posicio antiga per la posicio actual.
              Blanques.set(i, s.getAmazon(color, i));
          }
        }
    }
    
    

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !
        System.out.println("Bah! You are so slow...");
    }

    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     *
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return name;
    }
}
