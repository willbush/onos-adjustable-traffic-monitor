# onos-test-sdn-app
Testing ONOS Web GUI topology overlay

To run this you need ONOS setup and working. Read more about it here: https://wiki.onosproject.org

To build use maven and install using the onos-app:

    cd onos-test-sdn-app/
    mvn clean install
    onos-app $ONOS_IP install! target/tester-sample-1.0-SNAPSHOT.oar

$ONOS_IP is the ip on which onos is running if you are using the virtual machine I provided. Otherwise it might just be localhost or something different if you installed onos from scratch.

The oar filename may change above as it is generated when you build.

Also you can quickly reinstall the app after making a change to the source and building with the following command:

    onos-app $ONOS_IP reinstall! target/tester-sample-1.0-SNAPSHOT.oar
