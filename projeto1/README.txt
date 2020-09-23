SDIS 2019/2020 - 2º Semestre
Projeto 1 -- Serviço Backup Distribuido
________________________________________________________________________________________________________________________


COMPILAÇÃO
========================================================================================================================

Para compilar o projeto é necessário mudar o diretório atual para a pasta /src e executar:

    > javac *.java
________________________________________________________________________________________________________________________


EXECUÇÃO
========================================================================================================================

1. Iniciar os Peers

    Para iniciar os Peers basta abrir uma página do terminal por cada peer que pretende inicial na pasta /src e executar
    o seguinte comando:

        > java Peer <prt_version> <peerID> <peer_ap> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress> <MDRport>

        -prt_version: Versão do protocolo a executar (apenas 1.0 disponível).
        -peerID: Identificador único de um peer.
        -peer_ap: Ponto de acesso do peer no formato <nome>:<porta>
                        ::nome -> endereço IP ou um nome a ser usado no acesso ao objeto do RMI.
                        ::porta -> porta a ser usada pelo objeto do RMI (usar portas diferentes para peers diferentes).
        -MCaddress: Endereço IP do canal multicast de controlo.
        -MCport: Porta do canal multicast de controlo.
        -MDBaddress: Endereço IP do canal multicast de backup.
        -MDBport: Porta do canal multicast de backup.
        -MDRaddress: Endereço IP do canal multicast de restore.
        -MDRport: Porta do canal multicast de restore.

    Os endereços IP multicast debem estar entre os valores 224.0.0.0 e 224.0.0.255, que sao os endereços multicast
    reservados para redes locais.

    Exemplo para criação de 4 peers (correr cada um deles num terminal diferente):
        > java Peer 1.0 1 peer1:1101 224.0.0.0 8001 224.0.0.1 8002 224.0.0.2 8003
        > java Peer 1.0 2 peer2:1102 224.0.0.0 8001 224.0.0.1 8002 224.0.0.2 8003
        > java Peer 1.0 3 peer3:1103 224.0.0.0 8001 224.0.0.1 8002 224.0.0.2 8003
        > java Peer 1.0 4 peer4:1104 224.0.0.0 8001 224.0.0.1 8002 224.0.0.2 8003



2. Executar a TestApp

    Para executar a TestApp abrir uma nova página do terminal e executar:

        > java TestApp <peer_ap> <operation> <opnd_1> <opnd_2>

        -peer_ap: Ponto de acesso do peer no formato <nome>:<porta>   (igual ao do peer que queremos aceder).
                                          ::nome -> endereço IP ou um nome a ser usado no acesso ao objeto do RMI.
                                          ::porta -> porta a ser usada pelo objeto do RMI.
        -operation: Operação a ser executada (BACKUP, RESTORE, DELETE, RECLAIM ou STATE).
        -opnd_1: No caso de BACKUP, RESTORE ou DELETE -> diretorio/nome do ficheiro sobre o qual a operação atua.
                 No caso de RECLAIM -> espaço maximo de disco a ser usado pelo peer (em Kbytes; 1Kbyte = 1000bytes).
                 No caso de STATE -> não se aplica
        -opnd_2: No caso de BACKUP -> valor do replication degree.
                 Todos os outros casos -> não se aplica.

    Exemplos da execução da TestApp:
        > java TestApp peer1:1101 BACKUP resumo.pdf 2
        > java TestApp peer1:1101 BACKUP test1.jpg 3
        > java TestApp peer1:1101 DELETE resumo.pdf
        > java TestApp peer1:1101 RESTORE test1.jpg
        > java TestApp peer4:1104 RECLAIM 100
        > java TestApp peer2:1102 STATE
________________________________________________________________________________________________________________________


SCRIPTS DE DESENVOLVIMENTO DO PROJETO
========================================================================================================================

Para facilitar a execução do programa criei alguns scripts que funcionam em linux, sendo que o s_start.sh apenas
funciona com o Konsole instalado.
Para executar basta executar na pasta src:

    > ./script.sh <argumentos>

    Iniciar multiplos peers em janelas diferentes (Konsole):
        -> ./s_start.sh <numero_de_peers>

    Fazer BACKUP de um ficheiro num determinado peer:
        -> ./s_backup.sh <peer_id> <nome_do_ficheiro> <replication_degree>

    Fazer BACKUP de 4 ficheiros pré-definidos (back2.txt, resumo.pdf, test1.png, feup.png):
        -> ./s_multBackup.sh <peer_id> <replication_degree>

    Fazer RESTORE de um ficheiro num determinado peer:
        -> ./s_restore.sh <peer_id> <nome_do_ficheiro>

    Fazer RESTORE de 4 ficheiros pré-definidos (back2.txt, resumo.pdf, test1.png, feup.png):
        -> ./s_multBackup.sh <peer_id>

    Fazer DELETE de um ficheiro num determinado peer:
        -> ./s_delete.sh <peer_id> <nome_do_ficheiro>

    Fazer RECLAIM do espaço de disco de um peer (em kBytes; 1 Kbyte = 1000 bytes):
        -> ./s_reclaim.sh <peer_id> <espaço_desejado>

    Obter o STATE de um peer:
        -> ./s_state.sh <peer_id>

    Obter o STATE de vários peers (assumindo que foram iniciados com o primeiro script):
        -> ./s_multState.sh <numero_de_peers>


________________________________________________________________________________________________________________________


SCRIPTS DE APRESENTAÇÃO
========================================================================================================================

Para facilitar a apresentação do programa foram criados 4 scripts indicados pelo professor Pedro Souto:

    Compilação, a ser executado na pasta src:
        -> ./compile.sh 

    Criação de um peer, a ser executado na pasta src/build:
        -> ./peer.sh <prt_version> <peerID> <peer_ap> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress> <MDRport>

    Execução da classe TestApp, a ser executado na pasta src/build:
        -> ./test.sh <peer_ap> <operation> <opnd_1> <opnd_2>

    Apagar a pasta de um peer, a ser executado na pasta src/build:
        -> ./cleanup.sh <peer_id> 


________________________________________________________________________________________________________________________


CRÉDITOS
========================================================================================================================

Trabalho realizado por:
Ricardo Cardoso (up201604686)
