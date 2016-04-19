# onos-test-sdn-app

###Getting Things Running

To get this app working it requires ONOS to be setup and working. In addition, if you want to see this app actually do anything, then mininet needs to also be installed. Either use a VM that I provided that has everything already setup or set it up from scratch. Read more about installing ONOS from scratch [here](https://wiki.onosproject.org/display/ONOS/ONOS+from+Scratch) and mininet [here](http://mininet.org/download/). 

Note that if you do this from scratch, build ONOS from their sources since we are using their latest release. Regardless, this needs maven and Java JDK 8 to build anyway, so you should have everything required to build from source.

Steps to get this going is as follows:

```
cd
git clone https://github.com/willbush/onos-test-sdn-app.git
cd onos-test-sdn-app/
mvn clean install
onos-app $ONOS_IP install! target/tester-sample-1.0-SNAPSHOT.oar
```

The last step will fail unless you have ONOS up and running. The output should look like this if it correctly installs:

    {"name":"org.foo.app","id":84,"version":"1.0.SNAPSHOT","category":"UI","description":"ONOS OSGi UI Topology-View bundle archetype.","readme":"ONOS OSGi UI Topology-View bundle archetype.","origin":"Foo, Inc.","url":"http://onosproject.org","featuresRepo":"mvn:org.tester.app.sample/tester-sample/1.0-SNAPSHOT/xml/features","state":"ACTIVE","features":["tester-sample"],"permissions":[],"requiredApps":[]}

$ONOS_IP is the ip for onos if you are using the virtual machine I provided. Otherwise it might just be localhost or something different if you installed onos from scratch. Usually you can see the IP address it uses when ONOS boots up. It will say, `Creating local cluster configs for IP ...` and that is the IP you need to install to.

The oar filename may change above as it is generated when you build.

Also you can quickly reinstall the app after making a change to the source and building with the following command:

    onos-app $ONOS_IP reinstall! target/tester-sample-1.0-SNAPSHOT.oar

Now that you have the app installed, open the ONOS Web GUI by navigating to: [http://localhost:8181/onos/ui/login.html](http://localhost:8181/onos/ui/login.html)

The default username and password is: karaf

[See this page](https://wiki.onosproject.org/display/ONOS/The+ONOS+Web+GUI) which has a picture of the Web GUI. The Slide-out Topology Toolbar is what you need to click to find the button that enables this app.

However, there's no point enabling the app if there are no devices connected, so you need mininet up and running with a toplogy. I made a link on the VM desktop called "Setup Test Mininet Topo," click that if you are running the VM.

Otherwise, run: 
    sudo mn --custom /home/<YOUR_USER_NAME>/onos/tools/test/topos/tower.py --topo tower --controller remote,10.0.2.15 --mac

replace `<YOUR_USER_NAME>` with your user name. Also, if your ONOS IP differs from this one, replace the IP address with you ONOS IP address (described above). The path here is assuming you are running this on linux and have ONOS installed in your ~/ folder.

Now with the network tower toplogy up and running, ONOS should detect the devices and you should see the topology on the Web GUI. However, you probably only see the switches. if you hit the `/` or `\` key it will bring up a quick help. From there you can discover that the host visibility can be toggled with the `H` key. Hit that key and make sure it says the host are visible. Now if you still cannot see them, you probably need to do a `pingall` in mininet so that all the hosts are discovered. Now you should see a toplogy with switches and hosts.

Now click Slide-out Toplogy in the bottom left corner and click the icon that looks like a 4 pointed star. When you mouse over the icon it should say "Sample Meowster Topo Overlay," I have not taken the time to go through everything and give stuff sensable names yet. When you click that icon the bottom row (row below it) will change. These icons in the bottom row are like the different overlay settings for this app. The right-most icon "link mode" is really more like all-port trafic monitoring mode. The middle icon "mouse mode" is just from their Topo overlay example code, which I have been playing around with. The left most icon removes or "cancels" these overlays.

Click the "link mode" far right icon and the links between switches and hosts should turn green showing the traffic. You can `pingall` in mininet a few times to generage traffic or if you want to really send a lot of traffic through then use `iperf` in mininet. If you just type in that command it will select two hosts to send the traffic between, otherwise you can specify the hosts. Type `help iperf` in mininet for more info.

###IntelliJ development

You can import this source code the same way you import ONOS source code, which is outlined [here](https://wiki.onosproject.org/display/ONOS/Web+UI+Tutorial+-+Creating+a+Custom+View) (scroll down to "Import into IntelliJ" or [here](https://wiki.onosproject.org/display/ONOS/Importing+ONOS+projects+into+IntelliJ+IDEA).

To attach the debugger to the remote process follow [this guide](https://www.youtube.com/watch?v=UzWcI9KvP0g).

###Trouble Shooting ONOS

More than once I have run into issues where ONOS started acting strange. If you run into problems try closing ONOS and start it up again but with the "clean" parameter added like this: `ok clean`

Note that `ok` is an alias for `onos-karaf` that should be setup if `onos/tools/dev/bash_profile` was sourced correctly. If you look in that file you will see all the ONOS aliases they setup.

This will, as far as I know, uninstall all the apps you installed, and freshin other things up I'm assuming. This app will need to be installed again if you do this.

If worse comes to worse, go into your onos directory and `mvn clean install`, but be warned that this takes a long time to build. I have had issues getting the Web GUI to connect to the localhost, and this is how I resolved the issue.

