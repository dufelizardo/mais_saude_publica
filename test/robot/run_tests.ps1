# Runs this suite with all Robot Framework reports written to its own results/ folder,
# instead of wherever the command happens to be invoked from.
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
robot --outputdir "$here/results" @args "$here/test"
