/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 *  build notes
 *  -The reference CD to listen to while editing this file is
 *  nap:Cream+Live+2001+CD+2
 */

// place in the optional ant tasks package
// but in its own dotnet group

package org.apache.tools.ant.taskdefs.optional.dotnet;

// imports

import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;


/**
 *  Abstract superclass for dotnet compiler tasks.
 *
 *  History
 *  <table>
 *    <tr>
 *      <td>
 *        0.1
 *      </td>
 *      <td>
 *        First creation
 *      </td>
 *      <td>
 *        Most of the code here was copied verbatim from v0.3 of
 *        Steve Loughran's CSharp optional task. Abstracted functionality
 *        to allow subclassing of other dotnet compiler types.
 *      </td>
 *    </tr>
 *
 *  </table>
 *
 *
 * @author      Brian Felder bfelder@providence.org
 * @author      Steve Loughran
 * @version     0.1
 */

public abstract class DotnetCompile
         extends DotnetBaseMatchingTask {

    /**
     *  list of reference classes. (pretty much a classpath equivalent)
     */
    private String references;

    /**
     *  flag to enable automatic reference inclusion
     */
    private boolean includeDefaultReferences;

    /**
     *  icon for incorporation into apps
     */
    private File win32icon;

    /**
     *  icon for incorporation into apps
     */
    private File win32res;

    /**
     *  flag to control action on execution trouble
     */
    private boolean failOnError;

    /**
     *  using the path approach didnt work as it could not handle the implicit
     *  execution path. Perhaps that could be extracted from the runtime and
     *  then the path approach would be viable
     */
    private Path referenceFiles;

    /**
     *  optimise flag
     */
    private boolean optimize;

    /**
     * a list of definitions to support;
     */
    protected Vector definitionList = new Vector();

    /**
     * our resources
     */
    protected Vector resources = new Vector();

    /**
     *  Fix .NET reference inclusion. .NET is really dumb in how it handles
     *  inclusion. You have to list every 'assembly' -read DLL that is imported.
     *  So already you are making a platform assumption -shared libraries have a
     *  .dll;"+ extension and the poor developer has to know every library which
     *  is included why the compiler cant find classes on the path or in a
     *  directory, is a mystery. To reduce the need to be explicit, here is a
     *  long list of the core libraries used in Beta-1 of .NET ommitting the
     *  blatantly non portable (MS.win32.interop) and the .designer libraries.
     *  (ripping out Com was tempting) Casing is chosen to match that of the
     *  file system <i>exactly</i> so may work on a unix box too. there is no
     *  need to reference mscorlib.dll, cos it is always there
     */

    protected static final String[] DEFAULT_REFERENCE_LIST_DOTNET_10 = {"Accessibility.dll",
            "cscompmgd.dll",
            "CustomMarshalers.dll",
            "Mscorcfg.dll",
            "System.Configuration.Install.dll",
            "System.Data.dll",
            "System.Design.dll",
            "System.DirectoryServices.dll",
            "System.EnterpriseServices.dll",
            "System.dll",
            "System.Drawing.Design.dll",
            "System.Drawing.dll",
            "System.Management.dll",
            "System.Messaging.dll",
            "System.Runtime.Remoting.dll",
            "System.Runtime.Serialization.Formatters.Soap.dll",
            "System.Security.dll",
            "System.ServiceProcess.dll",
            "System.Web.dll",
            "System.Web.RegularExpressions.dll",
            "System.Web.Services.dll",
            "System.Windows.Forms.dll",
            "System.XML.dll"};

    protected static final String REFERENCE_OPTION= "/reference:";

    /**
     *  debug flag. Controls generation of debug information.
     */
    protected boolean debug;

    /**
     *  warning level: 0-4, with 4 being most verbose
     */
    private int warnLevel;

    /**
     *  main class (or null for automatic choice)
     */
    protected String mainClass;

    /**
     *  any extra command options?
     */
    protected String extraOptions;

    /**
     *  type of target. Should be one of exe|library|module|winexe|(null)
     *  default is exe; the actual value (if not null) is fed to the command
     *  line. <br>
     *  See /target
     */
    protected String targetType;

    /**
     *  utf out flag
     */

    protected boolean utf8output = false;

    /**
     *  list of extra modules to refer to
     */
    protected String additionalModules;
    /**
     * filesets of references
     */
    protected Vector referenceFilesets =new Vector();


    /**
     *  constructor inits everything and set up the search pattern
     */

    public DotnetCompile() {
        clear();
        setIncludes(getFilePattern());
    }

    /**
     *  reset all contents.
     */
    public void clear() {
        targetType = null;
        win32icon = null;
        srcDir = null;
        mainClass = null;
        warnLevel = 3;
        optimize = false;
        debug = true;
        references = null;
        failOnError = true;
        additionalModules = null;
        includeDefaultReferences = true;
        extraOptions = null;
    }


    /**
     * Semicolon separated list of DLLs to refer to.
     *
     *@param  s  The new References value
     */
    public void setReferences(String s) {
        references = s;
    }


    /**
     *  get the reference string or null for no argument needed
     *
     *@return    The References Parameter to CSC
     */
    protected String getReferencesParameter() {
        //bail on no references
        if (notEmpty(references)) {
            return REFERENCE_OPTION + references;
        } else {
            return null;
        }
    }

    /**
     * Path of references to include.
     * Wildcards should work.
     *
     *@param  path  another path to append
     */
    public void setReferenceFiles(Path path) {
        //demand create pathlist
        if (referenceFiles == null) {
            referenceFiles = new Path(this.getProject());
        }
        referenceFiles.append(path);
    }

    /**
     * add a new reference fileset to the compilation
     * @param reference
     */
    public void addReference(FileSet reference) {
        referenceFilesets.add(reference);
    }



    /**
     *  turn the path list into a list of files and a /references argument
     *
     *@return    null or a string of references.
     */
    protected String getReferenceFilesParameter() {
        //bail on no references
        if (references == null) {
            return null;
        }
        //iterate through the ref list & generate an entry for each
        //or just rely on the fact that the toString operator does this, but
        //noting that the separator is ';' on windows, ':' on unix
        String refpath = references.toString();

        //bail on no references listed
        if (refpath.length() == 0) {
            return null;
        }

        StringBuffer s = new StringBuffer(REFERENCE_OPTION);
        s.append(refpath);
        return new String(s);
    }


    /**
     *  get default reference list
     *
     *@return    null or a string of references.
     */
    protected String getDefaultReferenceParameter() {
        if (includeDefaultReferences) {
            StringBuffer s = new StringBuffer("/reference:");
            s.append(getDefaultReferenceList());
            return new String(s);
        } else {
            return null;
        }
    }


    /**
     * If true, automatically includes the common assemblies
     * in dotnet, and tells the compiler to link in mscore.dll.
     *
     *  set the automatic reference inclusion flag on or off this flag controls
     *  the string of references and the /nostdlib option in CSC
     *
     *@param  f  on/off flag
     */
    public void setIncludeDefaultReferences(boolean f) {
        includeDefaultReferences = f;
    }


    /**
     *  query automatic reference inclusion flag
     *
     *@return    true if flag is turned on
     */
    public boolean getIncludeDefaultReferences() {
        return includeDefaultReferences;
    }


    /**
     *  get the include default references flag or null for no argument needed
     *
     *@return    The Parameter to CSC
     */
    protected String getIncludeDefaultReferencesParameter() {
        return "/nostdlib" + (includeDefaultReferences ? "-" : "+");
    }



    /**
     * If true, enables optimization flag.
     *
     *@param  f  on/off flag
     */
    public void setOptimize(boolean f) {
        optimize = f;
    }


    /**
     *  query the optimise flag
     *
     *@return    true if optimise is turned on
     */
    public boolean getOptimize() {
        return optimize;
    }


    /**
     *  get the optimise flag or null for no argument needed
     *
     *@return    The Optimize Parameter to CSC
     */
    protected String getOptimizeParameter() {
        return "/optimize" + (optimize ? "+" : "-");
    }


    /**
     *  set the debug flag on or off.
     *
     *@param  f  on/off flag
     */
    public void setDebug(boolean f) {
        debug = f;
    }


    /**
     *  query the debug flag
     *
     *@return    true if debug is turned on
     */
    public boolean getDebug() {
        return debug;
    }


    /**
     *  get the debug switch argument
     *
     *@return    The Debug Parameter to CSC
     */
    protected String getDebugParameter() {
        return "/debug" + (debug ? "+" : "-");
    }


    /**
     * Level of warning currently between 1 and 4
     * with 4 being the strictest.
     *
     *@param  warnLevel  warn level -see .net docs for valid range (probably
     *      0-4)
     */
    public void setWarnLevel(int warnLevel) {
        this.warnLevel = warnLevel;
    }


    /**
     *  query warn level
     *
     *@return    current value
     */
    public int getWarnLevel() {
        return warnLevel;
    }


    /**
     *  get the warn level switch
     *
     *@return    The WarnLevel Parameter to CSC
     */
    protected String getWarnLevelParameter() {
        return "/warn:" + warnLevel;
    }


    /**
     *  Sets the name of main class for executables.
     *
     *@param  mainClass  The new MainClass value
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }


    /**
     *  Gets the MainClass attribute
     *
     *@return    The MainClass value
     */
    public String getMainClass() {
        return this.mainClass;
    }


    /**
     *  get the /main argument or null for no argument needed
     *
     *@return    The MainClass Parameter to CSC
     */
    protected String getMainClassParameter() {
        if (mainClass != null && mainClass.length() != 0) {
            return "/main:" + mainClass;
        } else {
            return null;
        }
    }


    /**
     * Any extra options which are not explicitly supported
     * by this task.
     *
     *@param  extraOptions  The new ExtraOptions value
     */
    public void setExtraOptions(String extraOptions) {
        this.extraOptions = extraOptions;
    }


    /**
     *  Gets the ExtraOptions attribute
     *
     *@return    The ExtraOptions value
     */
    public String getExtraOptions() {
        return this.extraOptions;
    }


    /**
     *  get any extra options or null for no argument needed
     *
     *@return    The ExtraOptions Parameter to CSC
     */
    protected String getExtraOptionsParameter() {
        if (extraOptions != null && extraOptions.length() != 0) {
            return extraOptions;
        } else {
            return null;
        }
    }


    /**
     * Set the destination directory of files to be compiled.
     *
     *@param  dirName  The new DestDir value
     */
    public void setDestDir(File dirName) {
        log( "DestDir currently unused", Project.MSG_WARN );
    }


    /**
     * set the target type to one of exe|library|module|winexe
     * @param targetType
     */
    public void setTargetType(TargetTypes targetType) {
        this.targetType=targetType.getValue();
    }
    /**
     * Set the type of target.
     *
     *@param  ttype          The new TargetType value
     *@exception  BuildException  if target is not one of
     *      exe|library|module|winexe
     */
    public void setTargetType(String ttype)
             throws BuildException {
        ttype = ttype.toLowerCase();
        if (ttype.equals("exe") || ttype.equals("library") ||
                ttype.equals("module") || ttype.equals("winexe")) {
            targetType = ttype;
        } else {
            throw new BuildException("targetType " + ttype
                    + " is not one of 'exe', 'module', 'winexe' or 'library'" );
        }
    }


    /**
     *  Gets the TargetType attribute
     *
     *@return    The TargetType value
     */
    public String getTargetType() {
        return targetType;
    }


    /**
     *  get the argument or null for no argument needed
     *
     *@return    The TargetType Parameter to CSC
     */
    protected String getTargetTypeParameter() {
        if (notEmpty(targetType)) {
            return "/target:" + targetType;
        } else {
            return null;
        }
    }


    /**
     *  Set the filename of icon to include.
     *
     *@param  fileName  path to the file. Can be relative, absolute, whatever.
     */
    public void setWin32Icon(File fileName) {
        win32icon = fileName;
    }


    /**
     *  get the argument or null for no argument needed
     *
     *@return    The Win32Icon Parameter to CSC
     */
    protected String getWin32IconParameter() {
        if (win32icon != null) {
            return "/win32icon:" + win32icon.toString();
        } else {
            return null;
        }
    }


    /**
     * Sets the filename of a win32 resource (.RES) file to include.
     * This is not a .NET resource, but what Windows is used to.
     *
     *@param  fileName  path to the file. Can be relative, absolute, whatever.
     */
    public void setWin32Res(File fileName) {
        win32res = fileName;
    }

    /**
     * Gets the file of the win32 .res file to include.
     * @return path to the file.
     */
    public File getWin32Res() {
        return win32res;
    }


    /**
     *  get the argument or null for no argument needed
     *
     *@return    The Win32Res Parameter to CSC
     */
    protected String getWin32ResParameter() {
        if (win32res != null) {
            return "/win32res:" + win32res.toString();
        } else {
            return null;
        }
    }


    /**
     * If true, require all compiler output to be in UTF8 format.
     *
     *@param  enabled  The new utf8Output value
     */
    public void setUtf8Output(boolean enabled) {
        utf8output = enabled;
    }


    /**
     *  Gets the utf8OutpuParameter attribute of the CSharp object
     *
     *@return    The utf8OutpuParameter value
     */
    protected String getUtf8OutputParameter() {
        return utf8output ? "/utf8output" : null;
    }




    /**
     * add a define to the list of definitions
     * @param define
     */
    public void addDefine(DotnetDefine define) {
        definitionList.addElement(define);
    }


    /**
     * get a list of definitions or null
     * @return a string beginning /D: or null for no definitions
     */
    protected String getDefinitionsParameter() throws BuildException {
        StringBuffer defines=new StringBuffer();
        Enumeration defEnum=definitionList.elements();
        while (defEnum.hasMoreElements()) {
            //loop through all definitions
            DotnetDefine define = (DotnetDefine) defEnum.nextElement();
            if (define.isSet(this)) {
                //add those that are set, and a delimiter
                defines.append(define.getValue(this));
                defines.append(getDefinitionsDelimiter());
            }
        }
        if (defines.length()==0) {
            return null;
        } else {
            return "/D:"+defines;
        }
    }


    /**
     * Semicolon separated list of modules to refer to.
     *
     *@param  params  The new additionalModules value
     */
    public void setAdditionalModules(String params) {
        additionalModules = params;
    }


    /**
     *  get the argument or null for no argument needed
     *
     *@return    The AdditionalModules Parameter to CSC
     */
    protected String getAdditionalModulesParameter() {
        if (notEmpty(additionalModules)) {
            return "/addmodule:" + additionalModules;
        } else {
            return null;
        }
    }


    /**
     *  get the argument or null for no argument needed
     *
     *@return    The OutputFile Parameter to CSC
     */
    protected String getDestFileParameter() {
        if (outputFile != null) {
            return "/out:" + outputFile.toString();
        } else {
            return null;
        }
    }


    /**
     * If true, fail on compilation errors.
     *
     *@param  b  The new FailOnError value
     */
    public void setFailOnError(boolean b) {
        failOnError = b;
    }


    /**
     *  query fail on error flag
     *
     *@return    The FailFailOnError value
     */
    public boolean getFailOnError() {
        return failOnError;
    }

    /**
     * link or embed a resource
     * @param resource
     */
    public void addResource(DotnetResource resource) {
        resources.add(resource);
    }

    /**
     *  test for a string containing something useful
     *
     *@param  s  string in
     *@return    true if the argument is not null or empty
     */
    protected boolean notEmpty(String s) {
        return s != null && s.length() != 0;
    }

    /**
     * validation code
     * @throws  BuildException  if validation failed
     */
    protected void validate()
            throws BuildException {
        if (outputFile != null && outputFile.isDirectory()) {
            throw new BuildException("destFile cannot be a directory");
        }
    }

    /**
     * Based on DEFAULT_REFERENCE_LIST and getReferenceDelimiter(),
     * build the appropriate reference string for the compiler.
     * @return The properly delimited reference list.
     */
    public String getDefaultReferenceList() {
        StringBuffer referenceList = new StringBuffer();
        for (int index = 0; index < DEFAULT_REFERENCE_LIST_DOTNET_10.length; index++) {
            referenceList.append(DEFAULT_REFERENCE_LIST_DOTNET_10[index]);
            referenceList.append(getReferenceDelimiter());
        }
        return referenceList.toString();
    }

    /**
     * Get the pattern for files to compile.
     * @return The compilation file pattern.
     */
    public String getFilePattern() {
        return "**/*." + getFileExtension();
    }


    /**
     *  do the work by building the command line and then calling it
     *
     *@throws  BuildException  if validation or execution failed
     */
    public void execute()
             throws BuildException {
        validate();
        NetCommand command = createNetCommand();
        //fill in args
        fillInSharedParameters(command);
        addResources(command);
        addCompilerSpecificOptions(command);
        int referencesOutOfDate=addReferenceFilesets(command,
                getOutputFileTimestamp());
        //if the refs are out of date, force a build.
        boolean forceBuild= referencesOutOfDate > 0;
        addFilesAndExecute(command, forceBuild);

    }

    /**
     * Get the delimiter that the compiler uses between references.
     * For example, c# will return ";"; VB.NET will return ","
     * @return The string delimiter for the reference string.
     */
    public abstract String getReferenceDelimiter();

    /**
     * Get the name of the compiler executable.
     * @return The name of the compiler executable.
     */
    public abstract String getCompilerExeName() ;

    /**
     * Get the extension of filenames to compile.
     * @return The string extension of files to compile.
     */
    public abstract String getFileExtension();


    /**
     * fill in the common information
     * @param command
     */
    protected void fillInSharedParameters(NetCommand command) {
        command.setFailOnError(getFailOnError());
        //fill in args
        command.addArgument("/nologo");
        command.addArgument(getAdditionalModulesParameter());
        command.addArgument(getDebugParameter());
        command.addArgument(getDefaultReferenceParameter());
        command.addArgument(getDefinitionsParameter());
        command.addArgument(getExtraOptionsParameter());
        command.addArgument(getMainClassParameter());
        command.addArgument(getOptimizeParameter());
        command.addArgument(getDestFileParameter());
        command.addArgument(getReferencesParameter());
        command.addArgument(getTargetTypeParameter());
        command.addArgument(getUtf8OutputParameter());
        command.addArgument(getWin32IconParameter());
        command.addArgument(getWin32ResParameter());
    }

    /**
     * for every resource declared, we get the (language specific)
     * resource setting
     */
    protected void addResources(NetCommand command) {
        Enumeration e=resources.elements();
        while (e.hasMoreElements()) {
            DotnetResource resource = (DotnetResource) e.nextElement();
            command.addArgument(createResourceParameter(resource));
        }
    }

    /**
     * from a resource, get the
     * @param resource
     * @return a string containing the resource param, or a null string
     * to conditionally exclude a resource.
     */
    protected abstract String createResourceParameter(DotnetResource resource);


    /**
     * run through the list of reference files and add them to the command
     * @param outputTimestamp timestamp to compare against
     * @return number of files out of date
     */

    protected int addReferenceFilesets(NetCommand command, long outputTimestamp) {
        int filesOutOfDate = 0;
        Hashtable filesToBuild=new Hashtable();
        for (int i = 0; i < referenceFilesets.size(); i++) {
            FileSet fs = (FileSet) referenceFilesets.elementAt(i);
            filesOutOfDate += command.scanOneFileset(
                    fs.getDirectoryScanner(getProject()),
                    filesToBuild,
                    outputTimestamp);
        }
        //bail out early if there were no files
        if (filesToBuild.size()==0) {
            return 0;
        }
        StringBuffer referenceList= new StringBuffer(REFERENCE_OPTION);
        //now scan the hashtable and add the files
        Enumeration files = filesToBuild.elements();
        while (files.hasMoreElements()) {
            File file = (File) files.nextElement();
            if (isFileManagedBinary(file)) {
                referenceList.append(file.toString());
                referenceList.append(getReferenceDelimiter());
            } else {
                log("ignoring "+file+" as it is not a managed executable",
                        Project.MSG_VERBOSE);
            }

        }
        //add it all to an argument
        command.addArgument(referenceList.toString());
        return filesOutOfDate;
    }

    /**
     * create our helper command
     * @return a command prefilled with the exe name and task name
     */
    protected NetCommand createNetCommand() {
        NetCommand command = new NetCommand(this, getTaskName(), getCompilerExeName());
        return command;
    }

    /**
     * add any compiler specifics
     * @param command
     */
    protected abstract void addCompilerSpecificOptions(NetCommand command);

    /**
     * override point for delimiting definitions
     * @return
     */
    public String getDefinitionsDelimiter() {
        return ";";
    }


    /**
     * test for a file being managed or not
     * @return true if we think this is a managed executable, and thus OK
     * for linking
     * @todo look at the PE header of the exe and see if it is managed or not.
     */
    protected static boolean isFileManagedBinary(File file) {
        String filename= file.toString().toLowerCase();
        return filename.endsWith(".exe") || filename.endsWith(".dll")
                || filename.endsWith(".netmodule");
    }

    /**
     * Target types to build.
     * valid build types are exe|library|module|winexe
     */
    public static class TargetTypes extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {
                "exe",
                "library",
                "module",
                "winexe"
            };
        }
    }


}


