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
 * @author Adrià Tort i Achraf Hmimou
 */
public class FinalChicken implements IPlayer, IAuto {

    String name;

    // profunditat
    int prof;
    boolean profunditatIterada;
    boolean timeout;

    // numero de nodes
    long nnodes;

    // taula de zobrist per representar al taulell
    long[][][] zobristTable;

    /**
     * Constructor buit. En aquest cas la profundització es farà de forma
     * iterada.
     */
    public FinalChicken() {
        this.name = "FinalChicken";

        // Com que no s'ha pasat cap parametre la profunditat es realitzarà de forma iterada fins a un timeout.
        this.profunditatIterada = true;
        this.timeout = false;
        this.nnodes = 0;

        // inicialització i generació de la taula zobrist
        this.zobristTable = new long[10][10][3];
        generaTaulaHash();
    }

    /**
     * Constructor parametritzat amb una profunditat determinada.
     *
     * @param profunditat El valor de la profunditat haurà de ser > 0
     */
    public FinalChicken(int profunditat) {
        this.name = "FinalChicken";

        // En quest cas la profunditat serà fixa, no iterada.
        this.profunditatIterada = false;
        this.prof = profunditat;
        this.timeout = false;

        this.nnodes = 0;

        // inicialització i generació de la taula zobrist
        this.zobristTable = new long[10][10][3];
        generaTaulaHash();
    }

    /**
     * Decideix el moviment donat un estat del taulell.
     *
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {

        // Inicialitzem sempre el timeout a false per cada moviment.
        this.timeout = false;

        // en m tindrem el moviment actual i en mAnterior tindrem el moviment anterior en cas de que hi hagi timout i la variable m estigui a null.
        Move m = null;
        Move mAnterior = null;

        //En cas de que la profunditat sigui iterada 
        if (profunditatIterada) {
            //comencarem amb una profunditat = 1
            this.prof = 1;
            // mentre no hi hagi timout
            while (!this.timeout) {

                // Ens guardaem sempre el moviment anterior 
                if (m != null) {
                    mAnterior = new Move(m.getAmazonFrom(), m.getAmazonTo(), m.getArrowTo(), (int) m.getNumerOfNodesExplored(), m.getMaxDepthReached(), m.getSearchType());
                }

                //Calcularem el moviment actual donat l'estat i la profunditat fixada
                m = movimentProfunditat(s);

                //Augmentem la profunditat en una unitat
                ++this.prof;
            }
        } // En cas de que la profunditat sigui fixada
        else {
            // Retornem el millor moviment fins a la profunditat fixada al constructor.
            m = movimentProfunditat(s);
        }

        // En cas de que no s'hagi calculat correctament el moviment degut al timeout, retornarem el moviment anterior. Sino retornarem el moviment actual.
        if (m.getAmazonFrom() == null) {
            return mAnterior;
        } else {
            return m;
        }
    }

    /**
     * Funcio que retorna el millor moviment trobat fins a una profunditat
     * determinada donat un estat del taulell utilitzant l'algorisme minimax.
     *
     * El que fem en aquesta funció es preparar les podes alfa i beta amb els
     * seus valors i la profunidat a la que arribarem (com a maxim). Despres
     * generarem totes les combinacions de moviments possibles i realitzarem el
     * minimax assegurant tenint sempre a la variable millorMoviment el moviment
     * més prometedor.
     *
     *
     * @param s estat determinat del taulell.
     * @return millor moviment fins a una profunditat determinada.
     */
    private Move movimentProfunditat(GameStatus s) {

        // reinicialitzzem cada vegada el numero de nodes que explorarem durant la crida del minimax.
        this.nnodes = 0;
        int Alpha = Integer.MIN_VALUE;
        int Beta = Integer.MAX_VALUE;

        // fixem la profunditat maxima que ens vindra donada desde el constructor parametritzat o de forma iterada desde la funcio Move.
        int profunditat = this.prof;

        // Aprofitem per calcular el valor del hash en el estat actual del taulell.
        //long hash = RecalcularHash(s);
        // Posarem el millor Moviment en una array on el primer element es el punt on es trobla l'amazona i el segon on mourem l'amazona i el tercer on colocarem la fletxa.
        Point[] millorMoviment = new Point[3];

        // També ens fa falta una array on guardarem els valors per cada moviment realitzat 
        Point[] moviment = new Point[3];

        // Per cada amazona del jugador
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {

            // Mirarem quins son els seus moviments possibles
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted == si es false revisa todas las posiciones, si es true solo las limite.
            for (int m = 0; m < MovimentsAmazona.size(); m++) { //Quitado timeout

                // Per cada moviment possible, mirarem quines son les caselles en las que podria posar una fletxa.
                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                // Per cada casella on podem posar la fletxa, generarem un estat del taulell.
                for (int c = 0; c < casellesBuides.size(); c++) { //quitado timeout

                    // Fem una copia del taulell on realitzarem els moviments.
                    GameStatus statusCopia = new GameStatus(s);

                    // Posicio on estroba la Amazona.
                    Point origen = s.getAmazon(s.getCurrentPlayer(), i);

                    // Posicio on mourem la Amazona.
                    Point desti = MovimentsAmazona.get(m);

                    // Posicio on posarem la fletxa.
                    Point fletxa = casellesBuides.get(c);

                    // Sobre l'estat copia del taulell que hem realitzem abans farem el moviment.
                    statusCopia.moveAmazon(origen, desti);
                    statusCopia.placeArrow(fletxa);
//                    
                    // Aprofitem per fer les diferents xors per obtenir el valor hash despres de haber fet el moviment complet.

//                    hash ^= this.zobristTable[origen.x][origen.y][s.getCurrentPlayer().ordinal() - 1];
//                    hash ^= this.zobristTable[desti.x][desti.y][s.getCurrentPlayer().ordinal() - 1];
//                    hash ^= this.zobristTable[fletxa.x][fletxa.y][CellType.ARROW.ordinal() - 1];
                    // Posarem el moviment en una array 
                    moviment[0] = origen;
                    moviment[1] = desti;
                    moviment[2] = fletxa;

                    // Ara cridarem al minimitzador amb l'estat on hem fet el moviment 
                    int eval = Minimitzador(statusCopia, profunditat - 1, Alpha, Beta);

                    // En cas de que sigui la millor heuristica trobada fins ara 
                    if (eval > Alpha) {
                        // guardarem la columna com a millorMoviment fins que un altre doni millor heuristica 
                        millorMoviment = moviment.clone();
                        // actualitzem Alfa amb la millor heurisitica que hem trobat de moment.
                        Alpha = eval;
                    }

                }
            }

        }

        // Aquest cas es dona quan tots els nodes explorats donan com heuristica -Infinit, per tant tots els moviments son perdedors i farem un moviments qualsevol perque ja hem perdut.
        if (millorMoviment[0] == null) {
            millorMoviment = moviment.clone();
        }

        // Retornem el objecte Move que també conte tota la informació obtinguda durant la exploracio.
        return new Move(millorMoviment[0], millorMoviment[1], millorMoviment[2], (int) this.nnodes, this.prof, SearchType.MINIMAX);
    }

    /**
     * Funció que s'encarrega d'establir la capa Maximitzadora on ha de retornar
     * el valor heuristic maxim obtingut d'evaluar tots els estats fills del
     * estat donat.
     *
     * @param s Estat determinat del taulell
     * @param Profunditat Profunditat maxima actual
     * @param Alpha Es tracta del valor que contindra el maxim heuristic actual
     * en les diferents crides.
     * @param Beta Es tracta del valor que contindra el minim exigible en les
     * diferents crides
     * @return retornarem la heuristica maxima obtinguda de evaluar als fills
     * del estat.
     */
    private int Maximitzador(GameStatus s, int Profunditat, int Alpha, int Beta) {

        // En cas de que en aquest estat el tinguem un guanyador i sugi el rival, hem perdut per tant retornem -infinit
        if (s.GetWinner() == CellType.opposite(s.getCurrentPlayer())) {
            return Integer.MIN_VALUE;
        }

        // Evaluem si en aquesta columna no es pot afegir mes fitxes o hem arribat a la profunditat maxima de exploracio. També mirarem si hi ha timout.
        if (Profunditat == 0 || this.timeout) {
            return -1 * getHeuristica(s, CellType.opposite(s.getCurrentPlayer()));                                 // Retornem la evaluacio heurisitca del taulell en aquesta situacio
        }

        // Per cada amazona del jugador
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {

            // Mirarem quins son els seus moviments possibles
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted ???
            for (int m = 0; m < MovimentsAmazona.size() && (!this.timeout); m++) {

                // Per cada moviment possible, mirarem quines son les caselles en las que podria posar una fletxa.
                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                // Per cada casella on podem posar la fletxa, generarem un estat del taulell.
                for (int c = 0; c < casellesBuides.size() && (!this.timeout); c++) {

                    // Fem una copia del taulell on realitzarem els moviments.
                    GameStatus statusCopia = new GameStatus(s);

                    // Posicio on estroba la Amazona.
                    Point origen = s.getAmazon(s.getCurrentPlayer(), i);

                    // Posicio on mourem la Amazona.
                    Point desti = MovimentsAmazona.get(m);

                    // Posicio on posarem la fletxa.
                    Point fletxa = casellesBuides.get(c);

                    // Sobre l'estat copia del taulell que hem realitzem abans farem el moviment.
                    statusCopia.moveAmazon(origen, desti);
                    statusCopia.placeArrow(fletxa);

                    // Cridarem a la evaluacio que fa el minimitzador amb una profunditat major.
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
     * Funcio que s'encarrega de establir la capa Minimitzadora don ha de
     * retornar el valor heuristic minim obtingut d'evaluar tots els estats
     * fills del estat donat.
     *
     * @param s Estat determinat del taulell
     * @param Profunditat Profunditat maxima actual
     * @param Alpha Es tracta del valor que contindra el maxim exigible
     * heuristic actual en les diferents crides.
     * @param Beta Es tracta del valor que contindra el minim en les diferents
     * crides
     * @return retornarem la heuristica minima obtinguda de evaluar als fills
     * del estat.
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

        // Per cada amazona del jugador
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {

            // Mirarem quins son els seus moviments possibles
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted ???
            for (int m = 0; m < MovimentsAmazona.size() && (!this.timeout); m++) {

                // Per cada moviment possible, mirarem quines son les caselles en las que podria posar una fletxa.
                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                // Per cada casella on podem posar la fletxa, generarem un estat del taulell.
                for (int c = 0; c < casellesBuides.size() && (!this.timeout); c++) {

                    // Fem una copia del taulell on realitzarem els moviments.
                    GameStatus statusCopia = new GameStatus(s);

                    // Posicio on estroba la Amazona.
                    Point origen = s.getAmazon(s.getCurrentPlayer(), i);

                    // Posicio on mourem la Amazona.
                    Point desti = MovimentsAmazona.get(m);

                    // Posicio on posarem la fletxa.
                    Point fletxa = casellesBuides.get(c);

                    // Sobre l'estat copia del taulell que hem realitzem abans farem el moviment.
                    statusCopia.moveAmazon(origen, desti);
                    statusCopia.placeArrow(fletxa);

                    // Cridarem a la evaluacio que fa el maximitzador amb una profunditat major.
                    int eval = Math.min(Beta, Maximitzador(statusCopia, Profunditat - 1, Alpha, Beta));
                    Beta = Math.min(Beta, eval);
                    // Comprobem si beta es menor que Alpha, aixo indicara que per aquesta branca no cal seguir i retornem Alpha com el maxim exigible del arbre.
                    if (Beta <= Alpha) {
                        return Alpha;
                    }
                }
            }

        }
        return Beta;   // Retornem Beta que sera el valor heuristic minim obtingut del proces.
    }

    /**
     * Funció que ens reitorna una llista amb totes les caselles buides del
     * taulell despres de moure una Amazona
     *
     * @param s Estat del taulel determinat
     * @param am Representa el integer que ens diu quina amazona sera
     * @param FIN Representa la posicio on mourem l'amazona.
     * @return retornem una llista amb totes les posicions buides del taulell on
     * potencialment podem posar una fletxa
     */
    private LinkedList<Point> casellasDisponibles(GameStatus s, int am, Point FIN) {

        LinkedList<Point> casellesBuides = new LinkedList<Point>();

        Point posicio;
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                posicio = new Point(i, j);
                // Si es una peca buida i no es la posicio on mourem l'amazona sera considerada com a disponible.
                if ((s.getPos(i, j) == CellType.EMPTY) && (!posicio.equals(FIN))) {
                    casellesBuides.add(posicio);
                }
            }
        }
        // Finalment afegim la posicio inical de l'amazona abans de fer el moviment ja que una vegada fet el moviment quedara buida.
        casellesBuides.add(s.getAmazon(s.getCurrentPlayer(), am));
        return casellesBuides;
    }

    public int getHeuristica(GameStatus s, CellType color) {
        //System.out.println("get heuristica");
        int puntuacio = 0;
        int puntuacioRival = 0;
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor(); i++) {
            
            //Primer jugador
            Point Amazona = s.getAmazon(color, i);
            puntuacio += evaluaTerritori(s, Amazona);
            //---------------------------------------------------------------------
            
            //Segon jugador
            Point AmazonaRival = s.getAmazon(CellType.opposite(color), i);
            puntuacioRival += evaluaTerritori(s, AmazonaRival);
            

        }

//        int movPossibles = 0;
//        int movPossiblesRival = 0;
//
//        ArrayList<Point> MovimentsAmazona;
//        ArrayList<Point> MovimentsAmazonaEnemigues;
//
//        for (int i = 0; i < s.getNumberOfAmazonsForEachColor(); i++) {
//            MovimentsAmazona = s.getAmazonMoves(s.getAmazon(color, i), false); // restricted 
//            movPossibles += MovimentsAmazona.size();
//            MovimentsAmazonaEnemigues = s.getAmazonMoves(s.getAmazon(CellType.opposite(color), i), false); // restricted
//            movPossiblesRival += MovimentsAmazonaEnemigues.size();
//            
        /**
         * int encontra = 0; int afavor = 0; int x; int y;
         *
         *
         * Point maleante = new Point(s.getAmazon(CellType.opposite(color), i));
         * Point honorable = new Point(s.getAmazon(color, i));
         *
         * for (x = -1; x < 2; x++){ for (y = -1; y<2; y++){
         * if(maleante.x+x>=0 && maleante.x+x<s.getSize() && maleante.y+y>=0 && maleante.y+y<s.getSize() && (s.getPos(maleante.x+x,maleante.y+y) != CellType.EMPTY) && (s.getPos(maleante.x+x,maleante.y+y) != CellType.opposite(color))){
         * ++afavor;
         * }
         * if(honorable.x+x>=0 && honorable.x+x<s.getSize() && honorable.y+y>=0
         * && honorable.y+y<s.getSize() &&
         * (s.getPos(honorable.x+x,honorable.y+y) != CellType.EMPTY) &&
         * (s.getPos(honorable.x+x,honorable.y+y) != color)){ ++encontra; }
         *
         * }
         * }
         * if(maleante.x == 0 || maleante.x == s.getSize()-1 || maleante.y == 0
         * || maleante.y == s.getSize()-1) afavor +=3; if(honorable.x == 0 ||
         * honorable.x == s.getSize()-1 || honorable.y == 0 || honorable.y ==
         * s.getSize()-1) encontra += 3;
         *
         *
         * //if(afavor==9) //System.out.println("se me va la pinza:
         * "+maleante.x+" "+maleante.y); //movPossibles += afavor*2;*
         */
//            }
        //movPossibles += Math.pow(2, afavor);
        //movPossiblesRival += Math.pow(2, encontra);
        //System.out.println("ficha ENEMIGA("+i+") encontra por "+afavor+" : ("+ maleante.x+" "+maleante.y+")");
        //System.out.println("ficha AMIGA("+i+") encontra por "+encontra+" : ("+ honorable.x+" "+honorable.y+")");
        //System.out.println(s.toString());
        //}
        ++this.nnodes;
        return (puntuacioRival - puntuacio);
    }
    
    private void imprimirTauler(int [][] a){
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                System.out.print(a[i][j] + " ");
            }
            System.out.print("\n");
        }
    }

    private ArrayList<Point> obtenirAdjacentsBuits(int[][] tauler, Point Posicio){
         ArrayList<Point> resultat = new ArrayList<>();
         int posX = Posicio.x;
         int posY = Posicio.y;
         Point a;
         for (int i = -1; i < 2; i++) {
             for (int j = -1; j < 2; j++) {
                 if((posX+i>=0 && posX+i<tauler.length) && (posY+j>=0 && posY+j<tauler.length) && tauler[posX+i][posY+j] == 0){
                      a = new Point(posX+i, posY+j);
                     resultat.add(a);
                 }
             }
            
        }
        return resultat;
    }
    
    private int evaluaTerritori(GameStatus s, Point Amazona) {
        //System.out.println("evalua territorio");
        //ArrayList< ArrayList<Point>> taulaTerritori = new ArrayList< >();

        int nMov = 1;
        int resultat = 0;
        int taulellComplet = s.getEmptyCellsCount();
        int nElementTerritori = 0;
        int[][] a = new int [s.getSize()][s.getSize()]; //suponemos que todo serán 0
        ArrayList<Point> Pendents = s.getAmazonMoves(Amazona, false);
        
        while(nElementTerritori < taulellComplet && Pendents.size()>0){
            System.out.println("nelement: "+nElementTerritori+" // "+taulellComplet);
            imprimirTauler(a);
            for (int i = 0; i<Pendents.size(); ++i){
                a[Pendents.get(i).x][Pendents.get(i).y] = nMov;
                resultat += nMov;
                ++nElementTerritori;
            }
            ArrayList<Point> Pendents_tmp = new ArrayList<>();


            for(int j = 0; j<Pendents.size(); ++j){
                ArrayList<Point> moviments = obtenirAdjacentsBuits(a, Pendents.get(j));//obtenirMovimentsDisponibles(s, Pendents.get(j), nMov);
                for(int k = 0; k<moviments.size() && (!Pendents_tmp.contains(moviments.get(k))); ++k){
                    Pendents_tmp.add(moviments.get(k));
                }
            }
            //Pendents = new ArrayList<>();
            Pendents = Pendents_tmp;
            ++nMov;

        }

        System.out.println("nelement FINAL: "+nElementTerritori+" // "+taulellComplet);
        imprimirTauler(a);
        return resultat;
        
        
       /** while (nElementTerritori < taulellCompleta) {
        
            if (nMovimentsPerPosicio == 1) {
                ArrayList<Point> moviments = s.getAmazonMoves(Amazona, false);
                taulaTerritori.add(moviments);
                nElementTerritori += moviments.size();
                if(moviments.isEmpty())
                    break;
            } 
            
            else if (nMovimentsPerPosicio > 1) {
                System.out.println("Amazonizador: "+Amazona);
                System.out.println("movposicio: "+nMovimentsPerPosicio);
                System.out.println("comptador: "+nElementTerritori+" // "+taulellCompleta);
                taulaTerritori.add(new ArrayList());
                for (int i = 0; i < taulaTerritori.get(nMovimentsPerPosicio - 2).size() && nElementTerritori < taulellCompleta; i++) {
                    Point posicio = taulaTerritori.get(nMovimentsPerPosicio - 2).get(i);
                    ArrayList<Point> moviments = obtenirMovimentsDisponibles(s, posicio);

                    for (int m = 0; m < moviments.size() && nElementTerritori < taulellCompleta; m++) {
                        System.out.println("moviment de m: "+moviments.get(m));
                        if (!taulaTerritori.get(nMovimentsPerPosicio - 2).contains(moviments.get(m))) {
                            taulaTerritori.get(nMovimentsPerPosicio - 1).add(moviments.get(m));
                            nElementTerritori++;
                        }
                    }
                }
            }
            
           // System.out.println("nElementTErriotri"+nElementTerritori);
            
            ++nMovimentsPerPosicio;
        }
        
        int evaluacio = 0;
        
        for(int e = 1; e <= taulaTerritori.size(); e++)
        {
            evaluacio += e * taulaTerritori.get(e-1).size();
        }
        
        return evaluacio;**/
    }
    
    private ArrayList<Point> obtenirMovimentsDisponibles(GameStatus s, Point Posicio)
    {

        //System.out.println("moviments disponbiles");
        ArrayList<Point> MovimentsDisponibles = new ArrayList<>();
        
        ArrayList<Point> dir = new ArrayList<>();
        
        for (int i = -1; i < 2; i++)
        {
            for(int j = -1; j<2; j++){
                if(!(i == 0 && j == 0))
                    dir.add(new Point(i,j));
            }
        }
        
        for(int i = 0; i<dir.size(); i++)
        {
           int posX = Posicio.x + dir.get(i).x;
           int posY = Posicio.y + dir.get(i).y;
           
           //TODO: Modificades variables posx i posy perque coincideixin amb >= 0
           while((posX>=0 && posX<s.getSize()) && (posY>=0 && posY<s.getSize()) && s.getPos(posX, posY) == CellType.EMPTY)
           {
               MovimentsDisponibles.add(new Point(posX, posY));
               posX+=dir.get(i).x;
               posY+=dir.get(i).y;
               
           }
        }
       //System.out.println("moviments sale");
       return MovimentsDisponibles;   
    }
    
    
    

    /**
     * Funció que s'encarrega de generar la taula de Zobrist que representarà
     * totes les configuracions del taulell.
     *
     */
    private void generaTaulaHash() {

        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 3; k++) {
                    this.zobristTable[i][j][k] = rand.nextLong();
                }
            }
        }
    }

    /**
     * Funció que itera sobre tot el taulell i retorna el valor hash que
     * representa l'estat del tauell.
     *
     * @param s Estat determinat del taulell
     * @return Retorna el valor hash obtingut que representa el estat del
     * taulell.
     */
    private long RecalcularHash(GameStatus s) {
        long h = 0;
        //TODO: Hacer variable el tamaño del tablero
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // Anirem fent acomuladament ela operació xor entre totes les peces no buides del tauell.
                if (s.getPos(i, j) != CellType.EMPTY) {
                    CellType pieza = s.getPos(i, j);
                    h ^= this.zobristTable[i][j][pieza.ordinal() - 1];
                }
            }
        }
        return h;
    }

    /**
     * Funció que ens avisa que hem de parar la cerca en curs perquè s'ha
     * exhaurit el temps de joc.
     */
    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !

        // En cas de que la profunditat sigui iterada activarem el timeout.
        if (profunditatIterada) {
            this.timeout = true;
        }
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
