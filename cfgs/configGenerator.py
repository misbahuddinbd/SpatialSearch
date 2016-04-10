__author__ = 'cseminhaj'

import os
cmd = "java -cp 'bin:libraries/djep-1.0.0.jar:libraries/jep-2.3.0.jar:libraries/sizeobj.jar' peersim.Simulator -f config.cfg"

networks = [512, 2048, 8192, 32768, 131072]
bounds = [16, 32, 64, 128, 256]
K=[25]
QKS = [0, 20, 40, 60, 80, 100]

index = 0
seed = 1234567890
network = networks[index]
bound = bounds[index]
k = K[0]
m = 1
qk = QKS
cycles = 1002

topology = 'sf'

config = ''

for knearest in qk:

    config = config+"#### constants ####"
    config = config+""+'\n'
    config = config+"SEED "+str(seed)+"\n"
    config = config+"NETWORK "+str(network)+"\n"
    config = config+"BOUND "+str(bound)+"\n"
    config = config+"K "+str(knearest)+"\n"
    config = config+"M "+str(m)+"\n"
    config = config+"QK "+str(knearest)+"\n"

    config = config+"#### general configurations ####"+"\n"
    config = config+""+'\n'
    config = config+"random.seed SEED"+'\n'
    config = config+"network.size NETWORK"+'\n'
    config = config+"network.node GeneralNode"+'\n'
    config = config+"simulation.endtime "+str(cycles)+'\n'

    config = config+"#### initializers ####"+'\n'
    config = config+""+'\n'
    config = config+"init.initializeCoordinates initializers.CoordinateInitializer"+'\n'
    config = config+"init.initializeCoordinates.protocol coordinateProtocol"+'\n'
    config = config+"init.initializeCoordinates.networksize NETWORK"+'\n'
    config = config+"init.initializeCoordinates.bound BOUND"+'\n'
    config = config+"init.initializeCoordinates.fileprefix coordinates"+'\n'
    config = config+"init.initializeCoordinates.foldername configstore"+'\n'
    config = config+""+'\n'

    config = config+"init.knngraphinitializer initializers.GraphInitializer"+'\n'
    config = config+"init.knngraphinitializer.protocol lnk"+'\n'
    config = config+"init.knngraphinitializer.coordinateProtocol coordinateProtocol"+'\n'
    config = config+"init.knngraphinitializer.networksize NETWORK"+'\n'
    config = config+"init.knngraphinitializer.bound BOUND"+'\n'
    config = config+"init.knngraphinitializer.fileprefix "+topology+"graph"+'\n'
    config = config+"init.knngraphinitializer.foldername configstore"+'\n'
    config = config+"init.knngraphinitializer.degree K"+'\n'
    config = config+""+'\n'

    config = config+"init.topologyobserver observers.TopologyObserver"+'\n'
    config = config+"init.topologyobserver.protocol lnk"+'\n'
    config = config+"init.topologyobserver.networksize NETWORK"+'\n'
    config = config+""+'\n'

    config = config+"init.initializegaptable initializers.GAPTableInitializer"+'\n'
    config = config+"init.initializegaptable.protocol indexProtocol"+'\n'
    config = config+"init.initializegaptable.linkable lnk"+'\n'
    config = config+"init.initializegaptable.coordinates coordinateProtocol"+'\n'
    config = config+"init.initializegaptable.networksize NETWORK"+'\n'
    config = config+"init.initializegaptable.bound BOUND"+'\n'
    config = config+"init.initializegaptable.fileprefix "+topology+"index"+'\n'
    config = config+"init.initializegaptable.foldername configstore"+'\n'
    config = config+""+'\n'

    config = config+"init.gaptableobserver observers.GAPTableObserver"+'\n'
    config = config+"init.gaptableobserver.linkable lnk"+'\n'
    config = config+"init.gaptableobserver.table_protocol indexProtocol"+'\n'
    config = config+"init.gaptableobserver.coordinates coordinateProtocol"+'\n'
    config = config+""+'\n'

    config = config+"init.initechoscheduler CDScheduler"+'\n'
    config = config+"init.initechoscheduler.protocol echo"+'\n'
    config = config+""+'\n'

    config = config+"#### controls ####"+'\n'
    config = config+""+'\n'

    config = config+"control.initechostate echoknn.InitEcho"+'\n'
    config = config+"control.initechostate.protocol echostate"+'\n'
    config = config+"control.initechostate.coord_protocol coordinateProtocol"+'\n'
    config = config+"control.initechostate.k QK"+'\n'
    config = config+"control.initechostate.step 1"+'\n'
    config = config+"control.initechostate.from 1"+'\n'
    config = config+""+'\n'

    config = config+"control.echoobserver echoknn.EchoObserver"+'\n'
    config = config+"control.echoobserver.protocol echostate"+'\n'
    config = config+"control.echoobserver.table_protocol indexProtocol"+'\n'
    config = config+"control.echoobserver.folder echoknn"+'\n'
    config = config+"control.echoobserver.netsize NETWORK"+'\n'
    config = config+"control.echoobserver.k QK"+'\n'
    config = config+"control.echoobserver.step 1"+'\n'
    config = config+"control.echoobserver.from 2"+'\n'
    config = config+""+'\n'

    config = config+"#### protocols ####"+'\n'
    config = config+""+'\n'

    config = config+"protocol.coordinateProtocol protocols.Coordinates"+'\n'
    config = config+""+'\n'

    config = config+"protocol.lnk IdleProtocol"+'\n'
    config = config+""+'\n'

    config = config+"protocol.indexProtocol protocols.GT"+'\n'
    config = config+""+'\n'

    config = config+"simulation.eventqueue utilities.OrderAwarePriorityQ"+'\n'
    config = config+""+'\n'

    config = config+"protocol.echostate echoknn.EchoState"+'\n'
    config = config+""+'\n'

    config = config+"protocol.invoke echoknn.Invoke"+'\n'
    config = config+"protocol.invoke.coord_protocol coordinateProtocol"+'\n'
    config = config+""+'\n'

    config = config+"protocol.tr UniformRandomTransport"+'\n'
    config = config+"protocol.tr.mindelay 0"+'\n'
    config = config+"protocol.tr.maxdelay 0"+'\n'
    config = config+""+'\n'

    config = config+"protocol.echo echoknn.Echo"+'\n'
    config = config+"protocol.echo.linkable lnk"+'\n'
    config = config+"protocol.echo.transport tr"+'\n'
    config = config+"protocol.echo.table_protocol indexProtocol"+'\n'
    config = config+"protocol.echo.state echostate"+'\n'
    config = config+"protocol.echo.coord_protocol coordinateProtocol"+'\n'
    config = config+"protocol.echo.step 1"+'\n'
    config = config+"protocol.echo.from 1"+'\n'
    config = config+""+'\n'

    config = config+"#### includes ####"+'\n'

    config = config+"include.init initializeCoordinates knngraphinitializer topologyobserver initializegaptable gaptableobserver initechoscheduler"+'\n'
    config = config+""+'\n'
    config = config+"include.protocol coordinateProtocol lnk indexProtocol tr echostate echo"+'\n'
    config = config+""+'\n'
    config = config+"include.control echoobserver initechostate"+'\n'
    config = config+""+'\n'
    wr = open('config.cfg','w')
    wr.write(config)
    wr.close()
    os.system(cmd)
