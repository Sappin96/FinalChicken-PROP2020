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
import edu.upc.epsevg.prop.amazons.SearchType;
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
    boolean timeout;
    long nnodes;
    long[][][] zobristTable;
    int juga1 = 0;
    int juga2 = 1;
    int creueta = 2;
    int buit = 3;

    public FinalChicken() {
        this.name = "FinalChicken";
        this.prof = 1;
        this.timeout = false;
        this.nnodes = 0;
        this.zobristTable = new long[10][10][4];

        generaTaulaHash();

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

        this.timeout = false;
        this.prof = 1;
        // ITERATIVE DEEPING:
        // ---
        // Poner el timeout y devolver en las funciones MINIMIZADOR I MAXIMIZADOR EN EL CASO BASE.

        // IMPLEMENTAR TIMEOUT
        // HASHING:
        // MEJORAR HAURSTICA:
        // DOCUMENTACION
        Move m = new Move(new Point(), new Point(), new Point(), 0, 0, SearchType.RANDOM);
        Move mAnterior = new Move(new Point(), new Point(), new Point(), 0, 0, SearchType.RANDOM);
        while (!timeout) {
            mAnterior = new Move(m.getAmazonFrom(), m.getAmazonTo(), m.getArrowTo(), (int) m.getNumerOfNodesExplored(), m.getMaxDepthReached(), m.getSearchType());
            Move t = movimentProfunditat(s);
            m = new Move(t.getAmazonFrom(), t.getAmazonTo(), t.getArrowTo(), (int) t.getNumerOfNodesExplored(), t.getMaxDepthReached(), t.getSearchType());
            ++this.prof;
        }

        if (m.getAmazonFrom() == null) {
            System.out.println("mAnterior");
            return mAnterior;
        } else {
            return m;
        }
    }

    private Move movimentProfunditat(GameStatus s) {

        int Alpha = Integer.MIN_VALUE;
        int Beta = Integer.MAX_VALUE;
        int profunditat = this.prof;
        this.nnodes = 0;
       // long hash = RecalcularHash(s);

        // Posarem el millor Moviment en una array on el primer element es el punt on es trobla  l'amazona i el segon on mourem l'amazona i el tercer on colocarem la fletxa.
        Point[] millorMoviment = new Point[3];
        Point[] moviment = new Point[3];

        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted == si es false revisa todas las posiciones, si es true solo las limite.
            for (int m = 0; m < MovimentsAmazona.size(); m++) { //Quitado timeout

                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                for (int c = 0; c < casellesBuides.size(); c++) { //quitado timeout
                    GameStatus statusCopia = new GameStatus(s);
                   // hash ^= this.zobristTable[statusCopia.getAmazon(statusCopia.getCurrentPlayer(), i).x][statusCopia.getAmazon(statusCopia.getCurrentPlayer(), i).y][IndexDe(statusCopia.getPos(statusCopia.getAmazon(statusCopia.getCurrentPlayer(), i)))];
                   // System.out.println(this.zobristTable[statusCopia.getAmazon(statusCopia.getCurrentPlayer(), i).x][statusCopia.getAmazon(statusCopia.getCurrentPlayer(), i).y][IndexDe(statusCopia.getPos(statusCopia.getAmazon(statusCopia.getCurrentPlayer(), i)))]);
                    statusCopia.moveAmazon(s.getAmazon(s.getCurrentPlayer(), i), MovimentsAmazona.get(m));
                   // hash ^= this.zobristTable[MovimentsAmazona.get(m).x][MovimentsAmazona.get(m).y][this.buit];
                    //System.out.println(hash);
                    statusCopia.placeArrow(casellesBuides.get(c));

                    // Posarem el millor moviment en una array 
                    moviment[0] = s.getAmazon(s.getCurrentPlayer(), i);
                    moviment[1] = MovimentsAmazona.get(m);
                    moviment[2] = casellesBuides.get(c);

                    // Es posible que en casos extremos no entre en 120 -> 123; eval nunca es mayor a alpha
                    int eval = Minimitzador(statusCopia, profunditat - 1, Alpha, Beta);
                    //System.out.println("EVAL / ALPHA: "+eval+" // "+Alpha);
                    if (eval > Alpha) {
                        millorMoviment = moviment.clone();                              // guardarem la columna com a millorMoviment fins que un altre doni millor heuristica 
                        Alpha = eval;                                           // actualitzem Alfa amb la millor heurisitica que hem trobat de moment.
                    }

                }
            }

        }

        System.out.println("Origen: " + millorMoviment[0]);
        System.out.println("Destino: " + millorMoviment[1]);
        System.out.println("Fletxa: " + millorMoviment[1]);

        if (millorMoviment[0] == null) {
            //TODO: REVISAR AMB BERNAT; Tirada tonta quan tots els moviments son perdedors
            millorMoviment = moviment.clone();
        }

        return new Move(millorMoviment[0], millorMoviment[1], millorMoviment[2], (int) this.nnodes, this.prof, SearchType.MINIMAX);
    }

    private int Maximitzador(GameStatus s, int Profunditat, int Alpha, int Beta) {

        // En cas de ser solucio, hem perdut. Vol dir que el ultim moviment (rival) ens ha guanyat.
        //  if(t.solucio(Columna, -color)) return Integer.MIN_VALUE;
        if (s.GetWinner() == CellType.opposite(s.getCurrentPlayer())) {
            return Integer.MIN_VALUE;
        }

        // Evaluem si en aquesta columna no es pot afegir mes fitxes o hem arribat a la profunditat maxima de exploracio
        if (Profunditat == 0 || this.timeout) {
            return -1 * getHeuristica(s, CellType.opposite(s.getCurrentPlayer()));                                 // Retornem la evaluacio heurisitca del taulell en aquesta situacio
        }

        // igual hay que cambiar el  4 luego !!!
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted ???
            for (int m = 0; m < MovimentsAmazona.size() && (!this.timeout); m++) {

                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                for (int c = 0; c < casellesBuides.size() && (!this.timeout); c++) {
                    GameStatus statusCopia = new GameStatus(s);
                    statusCopia.moveAmazon(s.getAmazon(s.getCurrentPlayer(), i), MovimentsAmazona.get(m));
                    statusCopia.placeArrow(casellesBuides.get(c));

                    int eval = Math.max(Alpha, Minimitzador(statusCopia, Profunditat - 1, Alpha, Beta));
                    Alpha = Math.max(Alpha, eval);
                    // Comprobem si beta es menor que Alpha, aixo indicara que per aquesta branca no cal seguir i retornem Beta com el minim exigible del arbre.
                    if (Beta <= Alpha) {
                        return Beta;
                    }
                }
            }
        }
        return Alpha;   // Retornem Alpha que sera el valor heuristic maxim obtingut del proces.
    }

    /**
     * Funcio que s'encarrega de establir la capa minimitzadora del algorisme i
     * obtenir la jugada amb pitjor heurisitca
     *
     * <p>
     * La funcio el que fa es iterar sobre els diferents moviments possibles i
     * obtenir la heurisitca minima obtinguda de compararlos tots.</p>
     *
     * <p>
     * El que es fa es principalment es valorar el cas base que es mirar si el
     * ultim moviment es perdedor, si es el cas retornem infinit ja que aixo
     * indica que hem guanyat. Un altre cas base eque es mirar si hem arribat a
     * la profunidat maxima o no hi ha moviment possible, en aquest cas mirarem
     * la heurisitca per aquell estan en concret. Finalment el cas recursiu es
     * iterar sobre els diferents moviments possibles i quedarnos amb la
     * heuristica minima de tots ells. </p>
     *
     *
     * @param t Es un objecte tipus taulell que correspon al ultim estat del
     * taulell.
     * @param color Es tracta de quin jugador li toca jugar... Si es 1 ets tu
     * mateix i si es -1 es el rival.
     * @param Alpha Es tracta del valor que contindra el maxim exigible
     * heuristic actual en les diferents crides.
     * @param Beta Es tracta del valor que contindra el minim en les diferents
     * crides
     * @return Es tracta de retornar la columna que correspon a la heuristica
     * minima.
     */
    private int Minimitzador(GameStatus s, int Profunditat, int Alpha, int Beta) {

        // En cas de ser solucio, hem perdut. Vol dir que el ultim moviment (rival) ens ha guanyat.
        //  if(t.solucio(Columna, -color)) return Integer.MIN_VALUE;
        if (s.GetWinner() == CellType.opposite(s.getCurrentPlayer())) {
            return Integer.MAX_VALUE;
        }

        // Evaluem si en aquesta columna no es pot afegir mes fitxes o hem arribat a la profunditat maxima de exploracio
        // MIRAR SI HEMOS ACABADO EL TIMEOUT.
        if (Profunditat == 0 || this.timeout) {
            return getHeuristica(s, CellType.opposite(s.getCurrentPlayer()));                                 // Retornem la evaluacio heurisitca del taulell en aquesta situacio
        }
        // igual hay que cambiar el  4 luego !!!
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted ???
            for (int m = 0; m < MovimentsAmazona.size() && (!this.timeout); m++) {

                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                for (int c = 0; c < casellesBuides.size() && (!this.timeout); c++) {
                    GameStatus statusCopia = new GameStatus(s);
                    statusCopia.moveAmazon(s.getAmazon(s.getCurrentPlayer(), i), MovimentsAmazona.get(m));

                    statusCopia.placeArrow(casellesBuides.get(c));

                    // Posarem el millor moviment en una array 
                    int eval = Math.min(Beta, Maximitzador(statusCopia, Profunditat - 1, Alpha, Beta));
                    Beta = Math.min(Beta, eval);
                    // Comprobem si beta es menor que Alpha, aixo indicara que per aquesta branca no cal seguir i retornem Beta com el minim exigible del arbre.
                    if (Beta <= Alpha) {
                        return Alpha;
                    }
                }
            }

        }
        return Beta;   // Retornem Alpha que sera el valor heuristic maxim obtingut del proces.
    }

    /**
     * Funció que ens reitorna una llista amb totes les caselles buides del
     * taulell
     */
    private LinkedList<Point> casellasDisponibles(GameStatus s, int am, Point FIN) {

        LinkedList<Point> casellesBuides = new LinkedList<Point>();

        Point peca;
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                peca = new Point(i, j);
                if ((s.getPos(i, j) == CellType.EMPTY) && (!peca.equals(FIN))) {
                    casellesBuides.add(peca);
                }
            }
        }
        casellesBuides.add(s.getAmazon(s.getCurrentPlayer(), am));
        return casellesBuides;
    }

    public int getHeuristica(GameStatus s, CellType color) {

        int movPossibles = 0;
        int movPossiblesRival = 0;

        ArrayList<Point> MovimentsAmazona;

        for (int i = 0; i < s.getNumberOfAmazonsForEachColor(); i++) {
            MovimentsAmazona = s.getAmazonMoves(s.getAmazon(color, i), false); // restricted 
            movPossibles += MovimentsAmazona.size();
            
            MovimentsAmazona = s.getAmazonMoves(s.getAmazon(CellType.opposite(color), i), false); // restricted
            movPossiblesRival += MovimentsAmazona.size();
            
            Point maleante = new Point(s.getAmazon(CellType.opposite(color), i));
            Point honorable = new Point(s.getAmazon(color, i));
            int encontra = 0;
            int afavor = 0;
            for (int x = -1; x < 2; x++){
                for (int y = -1; y<2; y++){
                    if(x!=0 && y!=0){
                        if(maleante.x+x>=0 && maleante.x+x<s.getSize() && maleante.y+y>=0 && maleante.y+y<s.getSize() && (s.getPos(maleante.x+x,maleante.y+y) != CellType.EMPTY)){
                            ++afavor;
                        }
                        if(honorable.x+x>=0 && honorable.x+x<s.getSize() && honorable.y+y>=0 && honorable.y+y<s.getSize() && (s.getPos(honorable.x+x,honorable.y+y) != CellType.EMPTY)){
                            ++encontra;
                        }
                    }
                }
            }
            movPossibles += afavor;
            movPossiblesRival += encontra;
        }
        
        
        
        ++this.nnodes;
        return (movPossibles - movPossiblesRival);
    }

    /**
     * Funció que s'encarrega de generar la taula de Zobrist
     */
    private void generaTaulaHash() {

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 3; k++) {
                    this.zobristTable[i][j][k] = (long) Math.random();
                }
            }
        }
    }

    /**
     * Funció que s'encarrega de recalcular el hash del tauler
     */
    private long RecalcularHash(GameStatus s) {
        long h = 0;
        //TODO: Hacer variable el tamaño del tablero
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (s.getPos(i, j) != CellType.EMPTY) {
                    CellType pieza = s.getPos(i, j);
                    int n; //P1 = 0, P2 = 1, ARR = 2, EMPT = 3
                    if (pieza == CellType.ARROW) {
                        n = this.creueta;
                    } else if (pieza == CellType.PLAYER1) {
                        n = this.juga1;
                    } else {
                        n = this.juga2;
                    }
                    h ^= this.zobristTable[i][j][n];
                }
            }
        }
        return h;
    }

    private int IndexDe(CellType a) {
        int n;
        if (a == CellType.PLAYER1) {
            n = this.juga1;
        } else if (a == CellType.PLAYER2) {
            n = this.juga2;
        } else if (a == CellType.ARROW) {
            n = this.creueta;
        } else {
            n = this.buit;
        }
        return n;
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !
        this.timeout = true;
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
