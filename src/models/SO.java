/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;
import models.Process;
/**
 *
 * @author jhona
 */
public class SO {
    
    public int quantum;
    public ArrayList<Process> list;

    public SO(int quantum) {
        this.quantum = quantum;
        list = new ArrayList<Process>();
    }
    
    public void addProcess(Process proc){
        list.add(proc);
    }   
    
}
