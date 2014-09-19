INSTALLATION

aql-format uses CodeMirror for formatting, which is not included in the package and must be added manually.

1. Download latest release (4.5 at the time of the writing) of CodeMirror from http://codemirror.net
2. Unzip downloaded codemirror.zip.
3. Create some directories inside top level directory:
	- aql-format/CodeMirror/addon/runmode
	- aql-format/CodeMirror/mode/javascript
4. Copy the following CodeMirror files to aql-format:
	- copy codemirror/addon/runmode/runmode-standalone.js to aql-format/CodeMirror/addon/runmode/runmode-standalone.js
	- for json highlight support, copy codemirror/mode/javascript/javascript.js to aql-format/CodeMirror/mode/javascript/javascript.js
	
USAGE

You need to include these resources in your html page:
    <script src="CodeMirror/addon/runmode/runmode-standalone.js"></script>
    <script src="CodeMirror/mode/aql/aql.js"></script>
    <script src="CodeMirror/mode/javascript/javascript.js"></script>
    <script src="aql-format.js"></script>

    <link rel="stylesheet" href="CodeMirror/theme/ehrexplorer.css">
	
Order of inclusion of scripts is important!

You create a formatter instance by:
	var formatter = new AqlFormatter();
	
supported operations:
	formatAql(inputAql): 
		formats an aql string (inputAql). Returns formatted string.
	highlightAql(inputAql, targetElement):
		outputs an aql string (inputAql) into a target DOM element (targetElement)
	highlightJson(inputJsom, targetElement):
		outputs an json string (inputJson) into a target DOM element (targetElement)

aql-format.html provides a simple usage example.