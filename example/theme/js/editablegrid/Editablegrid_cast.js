var myObject;
var editableGrid = new EditableGrid("DemoGrid");     

 

function cwmImportGrid(divId, url, readonly) {
	editableGrid.tableLoaded = function() { 
		if(readonly=="true") {
			for ( i=0; i < editableGrid.getColumnCount(); i++) {
				editableGrid.getColumn(i).editable = false;												
			}			
		}

		this.renderGrid(divId, "dataTable"); 
	}; //testgrid is class of table
	editableGrid.loadJSON(url);
	this.initializeGrid();

	//pull text down from URL
	var data = $.ajax({type: "GET", url: url, async: false}).responseText;;

	//parse string into object
	myObject = JSON.parse(data);


}

function cwmExportGrid(textAreaId) {
	jQuery('#'+textAreaId).val(JSON.stringify(myObject));  	
}


function initializeGrid() 
{
	
	with (this.editableGrid) {
		modelChanged = function(rowIndex, columnIndex, oldValue, newValue, row) {
			myObject.data[rowIndex].values[myObject.metadata[columnIndex].name] = newValue;
			
		};
	}
}

function cwmAddRow() {	
	var objectValue = {"id":myObject.data.length, "values":{"c1":"","c2":"","c3":"","c4":"","c5":""}};
	myObject.data.push(objectValue);
	editableGrid.append("Row" + myObject.data.length, objectValue, objectValue, true);
	synchronizeDataToMedataDimension();
}
function cwmRemoveRow() {
	editableGrid.remove(myObject.data.length - 1);
	myObject.data.splice(myObject.data.length - 1,1);
	synchronizeDataToMedataDimension();
}
function cwmAddColumn() {
	var nextColumnNumber = myObject.metadata.length + 1;
	var nextColumnLabel = "Column " + nextColumnNumber;
	var nextColumnName = "c" + nextColumnNumber;
	var newColumn = {"name":nextColumnName,"label":nextColumnLabel,"datatype":"string","editable":true};
	myObject.metadata.push(newColumn);
	editableGrid.addColumn();
	synchronizeDataToMedataDimension();
}
function cwmRemoveColumn() {	
	editableGrid.deleteColumn();
	myObject.metadata.splice(myObject.metadata.length - 1,1);
	synchronizeDataToMedataDimension();
}

function generateColumnNameByIndex(indexOfColumn) {
	var ColumnName = "c" + indexOfColumn;
	return ColumnName;
}

function synchronizeDataToMedataDimension() {
	for (i=0; i<myObject.data.length; i++) {
		//add missing data entries with empty string as default value
		for (j=0; j < myObject.metadata.length; j++) {				
			if (typeof myObject.data[i].values[generateColumnNameByIndex(j+1)] == 'undefined') {
				myObject.data[i].values[generateColumnNameByIndex(j+1)] = "";
			}
		}
		//remove excess data cells
		for (k=0; k < Object.keys(myObject.data[i].values).length - myObject.metadata.length; k++) {
			delete myObject.data[i].values[generateColumnNameByIndex(k+1+myObject.metadata.length)];
		}

	}	
}
