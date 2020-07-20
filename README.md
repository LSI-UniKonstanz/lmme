# LMME (**L**arge **M**etabolic **M**odel **E**xplorer)
LMME (Large Metabolic Model Explorer) is an Add-on for the open-source software [VANTED](www.vanted.org). It facilitates the exploration of large metabolic models by decomposing the large network into meaningful subsystems and by providing analytic techniques that may be applied successively.

![teaser-image](/images/app_teaser.png)

## Getting Started (Running the Application)
### Requirements
- You need a current version of the **Java runtime environment (JRE)** installed on your machine. You can download the latest version [here](https://www.java.com/de/download/) (for the development of LMME, we used [AdoptOpenJDK 11](https://adoptopenjdk.net/), which also includes a JRE).
- You also need a version of the **VANTED** software installed on your machine. You can download it [here](www.vanted.org) (for the development of LMME, we used Vanted version 2.7.1).
- If you would like to use the SBGN translation feature, you also need the **SBGN-ED Add-on** for Vanted. You can download it as jar file from the [SBGN-ED website](http://www.sbgn-ed.org) and install it in the same way as described for LMME below.
### Download & Installation
To install LMME, you need its jar file, which you can either download or build yourself.
#### Download the jar file
You can download the current version of **LMME** [here](https://www.cls.uni-konstanz.de/software/lmme/getting-started/).
#### Build the jar file yourself
Within Eclipse, run the **make.xml** file as an Ant build. The jar will be created in the root directory.
#### Installation
There are two ways to install **LMME**. Within Vanted, either use a drag&drop operation (drag the jar file from your systems file manager and drop it inside the Vanted program window) or navigate to *Edit* > *Preferences* > *Addon Manager* > *Install / Configure Add-ons* > *Install Add-on* and select the jar file in the appearing file chooser.
### First Steps
As soon as **LMME** has been installed into your Vanted program instance, you can load a metabolic model for its analysis. As before, you can either do that by using a drag&drop operation (drag an SBML file from your systems file manager and drop it inside the Vanted program window) or navigate to *File* > *Open* and select the SBML file in the appearing file chooser. An example model of Pseudomonas Aeruginosa PAO1 can be downloaded on the [LMME webpage](https://www.cls.uni-konstanz.de/software/lmme/getting-started/).

After loading the model, navigate to the LMME tab on the right and press the uppermost button Set model. You will immediately see some general information about the model, appearing at the top of the tab.

You can now select a decomposition method and press the Show Overview Graph button. This computes a decomposition and shows the overview graph on the left side of the application window. For a detailed description of the individual settings, see the Learn section.

As soon as the overview graph has been constructed, you can select some of its nodes and press the Show Selected Subsystems button at the bottom of the tab to have the consolidated subsystem graph shown on the right side of the application window.

If some errors occurred during the steps described above, or if you are interested in the individual settings or additional features that have not been covered above, please have a look at the [Learn section](https://www.cls.uni-konstanz.de/software/lmme/learn/) on the **LMME** webpage.

## Getting Started (Development)
1. Check out the [VANTED repository](https://github.com/LSI-UniKonstanz/vanted) and [VANTED libraries repository](https://github.com/LSI-UniKonstanz/vanted-libraries) (see also [here](https://github.com/LSI-UniKonstanz/vanted/wiki/Sourcecode) for more information) as well as the LMME repository and load them into Eclipse (or the IDE of your choice).
```
git checkout git@github.com:LSI-UniKonstanz/vanted.git
git checkout git@github.com:LSI-UniKonstanz/vanted-libraries.git
git checkout git@github.com:LSI-UniKonstanz/lmme.git
```    
1. Make sure the **lmme** project has its reference to the **Vanted** project (in Eclipse for example, go to *Project* > *Properties* > *Java build path* > *Projects* > *Add...* and add the **vanted** project there).
1. Start coding and finally build the jar as described above.

## Contact & Citation
If you have any questions, feedback, bug reports or feature requests, please do not hesitate to contact us via [E-Mail](mailto:michael.aichem@uni-konstanz.de).

If you use LMME in or for a scientific publication, please cite our latest publication:
> (citation currently under preparation)

## License
**LMME** is free software: you can redistribute it and/or modify it under the terms of the [GNU General Public License](https://www.gnu.org/licenses/) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
