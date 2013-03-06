ScaladocCHM
===========

> Scaladoc to CHM file


Requirements
------------

1. HTML Help Workshop

   Download: http://msdn.microsoft.com/library/windows/desktop/ms669985

2. Scala API document

   Download: http://www.scala-lang.org/downloads

   - http://www.scala-lang.org/downloads/distrib/files/scala-docs-2.10.0.zip
   - http://www.scala-lang.org/downloads/distrib/files/scala-docs-2.9.2.zip
   - http://www.scala-lang.org/downloads/distrib/files/scala-docs-2.9.3.zip

Usage
-----

1. download "HTML Help Workshop (Htmlhelp.exe)" and install.
2. download "Scala API document (scala-docs-2.10.0.zip)"
3. run cmd
4. `> git clone https://github.com/HKC/ScaladocCHM.git`
5. `> cd ScaladocCHM`
6. unzip `scala-docs-2.10.0.zip` to `./docs/scala-docs-2.10.0`
7. `> sbt assembly`
8. `> compile scala-docs-2.10.0`


CHM files (download)
--------------------

- scala-docs-2.10.0.chm: http://dl.dropbox.com/u/63505087/ScaladocCHM/scala-docs-2.10.0_20130212.chm.zip
- scala-docs-2.9.2.chm: http://dl.dropbox.com/u/63505087/ScaladocCHM/scala-docs-2.9.2_20130212.chm.zip
- scala-docs-2.9.3.chm: http://dl.dropbox.com/u/63505087/ScaladocCHM/scala-docs-2.9.3_20130306.chm.zip