/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import models.Process;
import models.SO;

/**
 *
 * @author jhona
 */
public class NewJFrame extends javax.swing.JFrame implements Runnable {

    /**
     * Creates new form NewJFrame
     */
    Thread hilo;
    int option = 0;

    //ArrayList de Procesos
    ArrayList<Process> listProcess;
    ArrayList<Process> listExecute = new ArrayList<Process>();
    ArrayList<Process> listBloqued = new ArrayList<Process>();
    ArrayList<Process> listPagMem = new ArrayList<Process>();
    ArrayList<JButton> listPagButtom = new ArrayList<JButton>();

    public SO sistemO = new SO(0);

    public void createSO(int timeQuantum) {
        sistemO = new SO(timeQuantum);
    }

    public void addProcess(Process proc) {
        sistemO.addProcess(proc);
    }

    public void startProcess() throws InterruptedException {
        ArrayList<Process> queue = new ArrayList<>();
        boolean aux = true;
        int timer = 0;
        int auxQuantum = sistemO.quantum;
        int auxPosition = 0;
        int quantum = 0;
        LinkedList<Process> listQueue = new LinkedList<Process>();
        sistemO.list = listExecute;

        do {
            timer++;
            quantum++;

            //Ingresa el proceso a la cola de procoesos en el momento de llegada
            for (Process process : sistemO.list) {
                if (process.timeArrival == timer) {
                    queue.add(process);
                    listQueue.addLast(process);
                    PaintQueueCpu(new ArrayList<Process>(listQueue));
                    paintBloqued(this.listExecute);
                    asingPag(new ArrayList<Process>(listQueue));
                }
            }

            //Resta una unidad de tiempo e proceoso al primer proceso de la cola de procesaieminto 
            queue.get(auxPosition).timeProcess = queue.get(auxPosition).timeProcess - 1;
            queue.get(auxPosition).state = 'P';
            asingPag(new ArrayList<Process>(listQueue));
            paintBloqued(this.listExecute);

            //Si existe un proceso bloqueado se genera un numero alateorio para determinar si se desbloquea y vuelve a la cola de proceso
            if (queue.get(auxPosition).state == 'B') {
                Process auxPro = queue.get(auxPosition);
                double auxInter = Math.random();
                if (auxInter < 0.50) {
                    queue.get(auxPosition).state = 'W';
                }
            }

            //Se representa la unidad procesada en el panel de CPU
            paintProcess(this.listExecute);
            PaintQueueCpu(new ArrayList<Process>(listQueue));
            paintBloqued(this.listExecute);
            Thread.sleep(jSliderVelocidad.getValue() * 2 * 10);
            JButton procces = new JButton();
            procces.setBackground(new Color(Integer.parseInt(queue.get(auxPosition).color.split(",")[0]), Integer.parseInt(queue.get(auxPosition).color.split(",")[1]), Integer.parseInt(queue.get(auxPosition).color.split(",")[2])));
            procces.setMinimumSize(new Dimension(20, 20));
            procces.setMaximumSize(new Dimension(20, 20));
            jPanelCpuProcess.add(procces);

            paintProcess(this.listExecute);
            PaintQueueCpu(new ArrayList<Process>(listQueue));
            paintBloqued(this.listExecute);

            //Control de velocidad de la simulacion
            Thread.sleep(jSliderVelocidad.getValue() * 2 * 10);

            System.out.println(quantum + " -- " + auxQuantum);
            System.out.println(queue.get(auxPosition).id + " -- " + queue.get(auxPosition).timeProcess);

            //Se genera un numero aleatorio para determinar la probabilidad de que un proceso que se este ejecutando se bloquee
            double auxInter = Math.random();
            if (listQueue.size() > 1) {
                if (auxInter < 0.40) {
                    queue.get(auxPosition).state = 'B';
                    queue.get(auxPosition).timeProcess = queue.get(auxPosition).timeProcess;
                    paintBloqued(this.listExecute);
                    asingPag(new ArrayList<Process>(listQueue));
                    auxPosition++;
                    if (auxPosition >= queue.size()) {
                        auxPosition = 0;
                    }

                }
            }

            //En caso de que el proceso finalice su ejecucion su estado pasara a finalizado y se sacara de la cola de procesamiento 
            if (queue.get(auxPosition).timeProcess <= 0) {
                queue.get(auxPosition).state = 'F';
                paintProcess(this.listExecute);
                listQueue.remove(queue.get(auxPosition));
                cleanPag(queue.get(auxPosition));
                queue.remove(queue.get(auxPosition));
                PaintQueueCpu(new ArrayList<Process>(listQueue));
                asingPag(new ArrayList<Process>(listQueue));
                paintBloqued(this.listExecute);

                System.out.println("termino proceso");

                auxPosition++;
                if (auxPosition >= queue.size()) {
                    auxPosition = 0;
                }
            }

            //En caso de que el quantum termine se pasa el proceso en ejecucion al final de la cola de proceso y se cambia su estado a esperano
            if (quantum == auxQuantum) {

                System.out.println("se cumplio el quantum");
                if (queue.size() > 0) {
                    Process auxProc = queue.get(auxPosition);
                    auxProc.state = 'W';
                    queue.get(auxPosition).state = 'W';
                    listQueue.remove(queue.get(auxPosition));
                    listQueue.addLast(auxProc);
                    paintProcess(this.listExecute);
                    PaintQueueCpu(new ArrayList<Process>(listQueue));
                    asingPag(new ArrayList<Process>(listQueue));
                    paintBloqued(this.listExecute);
                }
                auxPosition++;
                quantum = 0;
                if (auxPosition >= queue.size()) {
                    auxPosition = 0;
                }
            }

            //Cuando no queden elementos en la cola de procesado se dara por terminado la simualcion y el hilo se detendra. 
            System.out.println(":" + queue.size());
            if (queue.size() <= 0) {
                jPanelQueue2.removeAll();
                revalidate();
                repaint();
                hilo.stop();
                aux = false;
                break;
            }
        } while (aux);

    }

    public NewJFrame() {

        initComponents();
        setTitle("Simulador de Operaciones de un Sistema Operativo");
        this.getContentPane().setBackground(Color.WHITE);
        jPanelQueue.setLayout(new GridLayout(10, 1));
        jPanelbloqued.setLayout(new GridLayout(10, 1));
        jPanelQueue2.setLayout(new GridLayout(10, 1));
        jPanelMemoryPag.setLayout(new GridLayout(20, 1));
        jPanelCpuProcess.setLayout(new BoxLayout(jPanelCpuProcess, BoxLayout.LINE_AXIS));
        jPanel1.setBackground(Color.WHITE);
        jPanel2.setBackground(Color.WHITE);
        jPanel3.setBackground(Color.WHITE);
        jPanelCpuProcess.setBackground(Color.WHITE);
        jPanelQueue.setBackground(Color.WHITE);
        jPanelbloqued.setBackground(Color.WHITE);
        jPanelQueue2.setBackground(Color.WHITE);
        jPanelMemoryPag.setBackground(Color.WHITE);
        createMarc();

    }

    //Metodo para la creacion de los marcos para la paginacion, se crean 20 marcos.
    public void createMarc() {
        for (int i = 0; i < 20; i++) {
            JButton procces = new JButton("" + i);
            procces.setBackground(Color.white);
            procces.setName("v");
            listPagButtom.add(procces);
            revalidate();
            repaint();
        }
        for (int i = 0; i < listPagButtom.size(); i++) {
            if (listPagButtom.get(1).getName() == "v") {
                jPanelMemoryPag.add(listPagButtom.get(i));
            }
        }

    }

    //Realiza la segmentacion de los procesos en la cola de procesado y los pinta
    public void asingPag(ArrayList<Process> listProc) {
        int sizeMark = 4;

        for (int i = 0; i < listProc.size(); i++) {
            try {
                int auxCantidad = listProc.get(i).size / sizeMark;
                int paintCant = 0;
                for (int j = 0; j < listPagButtom.size(); j++) {
                    if (listProc.get(i).state == 'B') {
                        cleanPag(listProc.get(i));
                    }
                    //Revisa si en los marcos de paginacion existe el proceso, en caso de estar vacio usa ese espacio
                    if (coincidende(listProc.get(i)) != auxCantidad && listProc.get(i).state != 'B') {
                        if (listPagButtom.get(j).getName() == listProc.get(i).id) {
                            paintCant++;
                        }
                        if (listPagButtom.get(j).getName() == "v") {
                            listPagButtom.get(j).setBackground(new Color(Integer.parseInt(listProc.get(i).color.split(",")[0]), Integer.parseInt(listProc.get(i).color.split(",")[1]), Integer.parseInt(listProc.get(i).color.split(",")[2])));
                            listPagButtom.get(j).setName(listProc.get(i).id);
                            paintCant++;
                        }
                        if (paintCant >= auxCantidad) {
                            break;
                        }
                    }

                }

            } catch (Exception e) {
            }

        }
    }

    //Metodo para limpiar los marcos cuando un proceso deja la cola de procesado
    public void cleanPag(Process proc) {
        for (int i = 0; i < listPagButtom.size(); i++) {
            if (listPagButtom.get(i).getName() == proc.id) {
                listPagButtom.get(i).setName("v");
                listPagButtom.get(i).setBackground(Color.white);
            }
        }
    }

    public void paintProcess(ArrayList<Process> listProc) {

        jPanelQueue.removeAll();
        for (Iterator<Process> iterator = listProc.iterator(); iterator.hasNext();) {
            Process next = iterator.next();
            JButton procces = new JButton("" + next.state);
            procces.setForeground(Color.WHITE);
            procces.setFont(new Font("Times New Roman", Font.PLAIN, 20));
            procces.setBackground(new Color(Integer.parseInt(next.color.split(",")[0]), Integer.parseInt(next.color.split(",")[1]), Integer.parseInt(next.color.split(",")[2])));
            jPanelQueue.add(procces);
            revalidate();
            repaint();
        }

    }

    //Metodo para hallar cuantos bloques del proceso se han asignado a los marcos
    public int coincidende(Process proc) {
        int count = 0;
        for (int i = 0; i < listPagButtom.size(); i++) {
            if (listPagButtom.get(i).getName() == proc.id) {
                count++;
            }
        }
        return count;
    }

    public void PaintQueueCpu(ArrayList<Process> listProc) {

        jPanelQueue2.removeAll();
        for (Iterator<Process> iterator = listProc.iterator(); iterator.hasNext();) {

            Process next = iterator.next();
            if (next.state == 'P' || next.state == 'W') {
                JButton procces = new JButton("" + next.state);
                procces.setForeground(Color.WHITE);
                procces.setFont(new Font("Times New Roman", Font.PLAIN, 20));
                procces.setBackground(new Color(Integer.parseInt(next.color.split(",")[0]), Integer.parseInt(next.color.split(",")[1]), Integer.parseInt(next.color.split(",")[2])));
                jPanelQueue2.add(procces);
                revalidate();
                repaint();
            }

        }

    }

    public void paintBloqued(ArrayList<Process> listProc) {

        jPanelbloqued.removeAll();
        for (Iterator<Process> iterator = listProc.iterator(); iterator.hasNext();) {
            Process next = iterator.next();
            if (next.state == 'B') {
                JButton procces = new JButton("" + next.state);
                procces.setForeground(Color.WHITE);
                procces.setFont(new Font("Times New Roman", Font.PLAIN, 20));
                procces.setBackground(new Color(Integer.parseInt(next.color.split(",")[0]), Integer.parseInt(next.color.split(",")[1]), Integer.parseInt(next.color.split(",")[2])));
                jPanelbloqued.add(procces);
                revalidate();
                repaint();
            }

        }

    }

    public void paintSerie(ArrayList<Process> listProc) throws InterruptedException {

        for (Iterator<Process> iterator = listProc.iterator(); iterator.hasNext();) {
            Process next = iterator.next();

            startProcess();
            //next.state = 'F';
            paintProcess(this.listExecute);

        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelQueue = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jSliderVelocidad = new javax.swing.JSlider();
        jPanel5 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabelTiempo = new javax.swing.JLabel();
        jPanelQueue2 = new javax.swing.JPanel();
        addProcess = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButtonPlay = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jPanelCpuProcess = new javax.swing.JPanel();
        jPanelbloqued = new javax.swing.JPanel();
        jPanelMemoryPag = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        jPanelQueue.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cola de procesos", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jPanelQueue.setMaximumSize(new java.awt.Dimension(154, 600));
        jPanelQueue.setMinimumSize(new java.awt.Dimension(154, 600));
        jPanelQueue.setPreferredSize(new java.awt.Dimension(154, 600));

        javax.swing.GroupLayout jPanelQueueLayout = new javax.swing.GroupLayout(jPanelQueue);
        jPanelQueue.setLayout(jPanelQueueLayout);
        jPanelQueueLayout.setHorizontalGroup(
            jPanelQueueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelQueueLayout.setVerticalGroup(
            jPanelQueueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 575, Short.MAX_VALUE)
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Parametros", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        jSliderVelocidad.setBackground(new java.awt.Color(255, 255, 255));
        jSliderVelocidad.setForeground(new java.awt.Color(255, 255, 255));
        jSliderVelocidad.setMaximum(50);
        jSliderVelocidad.setValue(1);
        jPanel4.add(jSliderVelocidad);

        jPanel5.setLayout(new java.awt.GridLayout(1, 0));

        jButton3.setText("Limpiar");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });
        jPanel5.add(jButton3);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tiempo", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));
        jPanel6.setLayout(new java.awt.GridBagLayout());

        jLabelTiempo.setBackground(new java.awt.Color(255, 255, 255));
        jLabelTiempo.setFont(new java.awt.Font("Times New Roman", 0, 36)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 308, 44, 310);
        jPanel6.add(jLabelTiempo, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 849, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelQueue2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Memoria Principal", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jPanelQueue2.setMaximumSize(new java.awt.Dimension(154, 600));
        jPanelQueue2.setMinimumSize(new java.awt.Dimension(154, 600));
        jPanelQueue2.setName(""); // NOI18N
        jPanelQueue2.setPreferredSize(new java.awt.Dimension(154, 600));

        javax.swing.GroupLayout jPanelQueue2Layout = new javax.swing.GroupLayout(jPanelQueue2);
        jPanelQueue2.setLayout(jPanelQueue2Layout);
        jPanelQueue2Layout.setHorizontalGroup(
            jPanelQueue2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 126, Short.MAX_VALUE)
        );
        jPanelQueue2Layout.setVerticalGroup(
            jPanelQueue2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 575, Short.MAX_VALUE)
        );

        addProcess.setText("Add");
        addProcess.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addProcessMouseClicked(evt);
            }
        });
        addProcess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProcessActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jButtonPlay.setText("Iniciar Proceso");
        jButtonPlay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonPlayMouseClicked(evt);
            }
        });
        jButtonPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlayActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonPlay);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("CPU"));

        javax.swing.GroupLayout jPanelCpuProcessLayout = new javax.swing.GroupLayout(jPanelCpuProcess);
        jPanelCpuProcess.setLayout(jPanelCpuProcessLayout);
        jPanelCpuProcessLayout.setHorizontalGroup(
            jPanelCpuProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelCpuProcessLayout.setVerticalGroup(
            jPanelCpuProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelCpuProcess, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(jPanelCpuProcess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(86, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel3);

        jPanelbloqued.setBorder(javax.swing.BorderFactory.createTitledBorder("Memoria secundaria"));
        jPanelbloqued.setMaximumSize(new java.awt.Dimension(134, 600));
        jPanelbloqued.setMinimumSize(new java.awt.Dimension(134, 600));

        javax.swing.GroupLayout jPanelbloquedLayout = new javax.swing.GroupLayout(jPanelbloqued);
        jPanelbloqued.setLayout(jPanelbloquedLayout);
        jPanelbloquedLayout.setHorizontalGroup(
            jPanelbloquedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 106, Short.MAX_VALUE)
        );
        jPanelbloquedLayout.setVerticalGroup(
            jPanelbloquedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelMemoryPag.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Marcos Mem", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        jPanelMemoryPag.setMaximumSize(new java.awt.Dimension(154, 600));
        jPanelMemoryPag.setMinimumSize(new java.awt.Dimension(154, 600));

        javax.swing.GroupLayout jPanelMemoryPagLayout = new javax.swing.GroupLayout(jPanelMemoryPag);
        jPanelMemoryPag.setLayout(jPanelMemoryPagLayout);
        jPanelMemoryPagLayout.setHorizontalGroup(
            jPanelMemoryPagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelMemoryPagLayout.setVerticalGroup(
            jPanelMemoryPagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel1.setText("Tamaño de marco: 4");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(28, 28, 28)
                .addComponent(jPanelQueue2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelbloqued, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addProcess, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(77, 77, 77)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelMemoryPag, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanelQueue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                            .addComponent(jPanelbloqued, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelQueue2, javax.swing.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                            .addComponent(jPanelMemoryPag, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelQueue2.getAccessibleContext().setAccessibleName("Cola de Ejecucion");
        jPanelQueue2.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addProcessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProcessActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addProcessActionPerformed

    private void addProcessMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addProcessMouseClicked
        listExecute.add(new Process(Integer.parseInt(JOptionPane.showInputDialog("Tiempo de proceso")), Integer.parseInt(JOptionPane.showInputDialog("Tiempo de llegada")), Integer.parseInt(JOptionPane.showInputDialog("Tamaño del Proceso"))));
        paintProcess(listExecute);
    }//GEN-LAST:event_addProcessMouseClicked

    private void jButtonPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonPlayActionPerformed

    private void jButtonPlayMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonPlayMouseClicked

        option = 1;
        sistemO.quantum = Integer.parseInt(JOptionPane.showInputDialog("Quantum"));
        hilo = new Thread(this);
        hilo.start();


    }//GEN-LAST:event_jButtonPlayMouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        jPanelCpuProcess.removeAll();
        jPanelQueue.removeAll();
        jPanelQueue2.removeAll();
        sistemO.list.clear();
        //user1 = new User("User 1");
        // user2 = new User("User 1");
        repaint();
        revalidate();
    }//GEN-LAST:event_jButton3MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addProcess;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButtonPlay;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelTiempo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanelCpuProcess;
    private javax.swing.JPanel jPanelMemoryPag;
    private javax.swing.JPanel jPanelQueue;
    private javax.swing.JPanel jPanelQueue2;
    private javax.swing.JPanel jPanelbloqued;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderVelocidad;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {

        try {
            if (option == 1) {
                paintSerie(listExecute);
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
