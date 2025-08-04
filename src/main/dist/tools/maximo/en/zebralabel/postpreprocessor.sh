#!/bin/bash
export SMP_HOME=/opt/IBM/SMP
export TOOLS_HOME=$SMP_HOME/maximo/tools/maximo
export MAXIMO_HOME=$SMP_HOME/maximo/applications/maximo
export CLASSPATH=.:$MAXIMO_HOME/businessobjects/classes:$TOOLS_HOME/classes:$TOOLS_HOME
export WLP_BUILD_HOME=$SMP_HOME/maximo/deployment/was-liberty-default
export PRODUCT_HOME=$SMP_HOME/maximo/deployment/product
export MAXIMO_EAR_NAME=buildmaximoear.xml
export MAXIMO_BUILD_EAR_NAME=buildmaximoear-build.xml

# Update the build XML with the product XML files from the legacy build folder.
function updateBuildXml(){
    local ear_build_xml=$1
    echo "Processing $ear_build_xml"

    shell_script_file="${ear_build_xml%.*}.sh"

    # If the ear build script file is not calling the 'psdi.tools.MaximoBuildEarTask then manually update it.
    if [[ "$ear_build_xml" = *"maximo-all.xml" ]] || [[ "$ear_build_xml" = *"buildmaximojmsconsumer-ear.xml" ]] || [[ "$ear_build_xml" = *"buildmaximomea-ear.xml" ]]; then
      echo "The file $shell_script_file does not contain the psdi.tools.MaximoBuildEarTask, manually running the product task."
      mkdir -p $WLP_BUILD_HOME/tmp
      cp "$ear_build_xml" $WLP_BUILD_HOME/tmp/$MAXIMO_EAR_NAME
      cp -r $PRODUCT_HOME $WLP_BUILD_HOME/tmp

      "$JAVA_HOME"/bin/java -classpath $CLASSPATH psdi.tools.MaximoBuildEarTask -k$WLP_BUILD_HOME/tmp

      echo "Copying result to $ear_build_xml"
      cp -f $WLP_BUILD_HOME/tmp/$MAXIMO_BUILD_EAR_NAME "$ear_build_xml"

      rm -rf $WLP_BUILD_HOME/tmp
    fi
}

# Loop through the build XML files and look for each build file that creates an EAR.
# For each file that generates an ear file, check the corresponding shell script to see if it calls psdi.tools.MaximoBuildEarTask
find "$WLP_BUILD_HOME" -type f -iname "*.xml" | while read -r FILE; do
    if grep -q '<ear' "$FILE"; then
        if ! (grep -q 'psdi.tools.MaximoBuildEarTask' "$FILE"); then
            updateBuildXml "$FILE"
        fi
    fi
done
