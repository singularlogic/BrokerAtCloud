var kb, kbsd;
function deleteQualitativeTables() {
    var anchor = document.getElementById("qualvalues");
    anchor.innerHTML = "";
    // tbd
}
function deactivateInputFields() {
    document.getElementById("bp_namespace").disabled = true;
    document.getElementById("bp_business_entity").disabled = true;
    document.getElementById("bp_legal_name").disabled = true;
    document.getElementById("bp_instance").disabled = true;
    document.getElementById("bp_model").disabled = true;
    document.getElementById("slp_class").disabled = true;
    document.getElementById("classtaxpref").disabled = true;
    document.getElementById("classtaxroot").disabled = true;
    document.getElementById("classtaxURI").disabled = true;
    document.getElementById("valid_from").disabled = true;
    document.getElementById("valid_through").disabled = true;
    document.getElementById("successor_of_bp").disabled = true;
    document.getElementById("recommendation_deprecated_from").disabled = true;
    document.getElementById("onboarding_deprecated_from").disabled = true;
    //deactivateQuantSLSchemaTable();
    //document.getElementById("quant_sl_table_add").disabled = true;
    //document.getElementById("quant_sl_table_delete").disabled = true;
    deactivateQualSLSchemaTable();
    //document.getElementById("qual_sl_table_add").disabled = true;
    //document.getElementById("qual_sl_table_delete").disabled = true;
    deactivateClassTaxTable();
    //document.getElementById("classtax_table_add").disabled = true;
    //document.getElementById("classtax_table_delete").disabled = true;
    //document.getElementById("safe_schemas").disabled = true;
    //document.getElementById("edit_schemas").disabled = false;
}
/*function deactivateQuantSLSchemaTable() {
 var quant_sl_table = document.getElementById("quantserviceleveldetails");
 for (i = 1; i < quant_sl_table.rows.length; i++) {
 quant_sl_table.rows[i].cells[2].childNodes[0].disabled = true;
 quant_sl_table.rows[i].cells[3].childNodes[0].disabled = true;
 quant_sl_table.rows[i].cells[4].childNodes[0].disabled = true;
 quant_sl_table.rows[i].cells[5].childNodes[0].disabled = true;
 quant_sl_table.rows[i].cells[6].childNodes[0].disabled = true;
 quant_sl_table.rows[i].cells[7].childNodes[0].disabled = true;
 quant_sl_table.rows[i].cells[8].childNodes[0].disabled = true;
 quant_sl_table.rows[i].cells[9].childNodes[0].disabled = true;
 }
 }*/
function deactivateQualSLSchemaTable() {
    var qual_sl_table = document.getElementById("qualserviceleveldetails");
    for (i = 1; i < qual_sl_table.rows.length; i++) {
        qual_sl_table.rows[i].cells[0].childNodes[0].disabled = true;
        qual_sl_table.rows[i].cells[1].childNodes[0].disabled = true;
    }
}
function deactivateClassTaxTable() {
    var qual_sl_table = document.getElementById("classtaxconcepts");
    for (i = 1; i < qual_sl_table.rows.length; i++) {
        qual_sl_table.rows[i].cells[0].childNodes[0].disabled = true;
        qual_sl_table.rows[i].cells[1].childNodes[0].disabled = true;
        qual_sl_table.rows[i].cells[2].childNodes[0].disabled = true;
    }
}
function allowDrop(event) {
    event.preventDefault();
}
function findBase(kb) {
    var base = kb.databank.baseURI
    var services = kb.where("?brokerPolicy a ?serviceModel")
            .where("?serviceModel rdfs:subClassOf usdl-core:ServiceModel")
            .where("?brokerPolicy usdl-core-cb:validFrom ?validFrom");
    services.each(function () {
        var selection_class = this["serviceModel"];
        var selection_instance = this["brokerPolicy"];
        var validFrom = this["validFrom"];
        var url_class = selection_class.value._string;
        var url_instance = selection_instance.value._string;
        base = url_class.split('#')[0];
        document.getElementById("bp_namespace").value = base;
        // Assign the service model class and instance to the business policy form
        document.getElementById("bp_model").value = url_class.split('#')[1];
        document.getElementById("bp_instance").value = url_instance.split('#')[1];
        document.getElementById("valid_from").value = validFrom.value;
    })
    var validthrough = kb.where("?brokerPolicy a ?serviceModel")
            .where("?serviceModel rdfs:subClassOf usdl-core:ServiceModel")
            .where("?brokerPolicy usdl-core-cb:validThrough ?validThrough");
    validthrough.each(function () {
        var validThrough = this["validThrough"];
        document.getElementById("valid_through").value = validThrough.value;
    })
    var successor = kb.where("?brokerPolicy a ?serviceModel")
            .where("?serviceModel rdfs:subClassOf usdl-core:ServiceModel")
            .where("?brokerPolicy gr:successorOf ?successor");
    successor.each(function () {
        var successor_of = this["successor"];
        document.getElementById("successor_of_bp").value = successor_of.value._string;
    })
    var onboarding = kb.where("?brokerPolicy a ?serviceModel")
            .where("?serviceModel rdfs:subClassOf usdl-core:ServiceModel")
            .where("?brokerPolicy gr:successorOf ?successor")
            .where("?brokerPolicy usdl-core-cb:deprecationOnboardingTimePoint ?onboarding");
    onboarding.each(function () {
        var onboarding_date = this["onboarding"];
        document.getElementById("onboarding_deprecated_from").value = onboarding_date.value;
    })
    var recommendation = kb.where("?brokerPolicy a ?serviceModel")
            .where("?serviceModel rdfs:subClassOf usdl-core:ServiceModel")
            .where("?brokerPolicy gr:successorOf ?successor")
            .where("?brokerPolicy usdl-core-cb:deprecationRecommendationTimePointBP ?recommendation");
    recommendation.each(function () {
        var recommendation_date = this["recommendation"];
        document.getElementById("recommendation_deprecated_from").value = recommendation_date.value;
    })
    var business_entity = kb.where("?bentity a gr:BusinessEntity")
            .where("?bentity gr:legalName ?legalName")
    business_entity.each(function () {
        var bizentity = this["bentity"];
        var legalname = this["legalName"];
        var url_bizentity = bizentity.value._string;
        // Assign the service model class and instance to the business policy form
        document.getElementById("bp_business_entity").value = url_bizentity.split('#')[1];
        document.getElementById("bp_legal_name").value = legalname.value;
    })
    return base
}
function addConcept() {
    var table = document.getElementById("class_table");
    var rowCount = table.rows.length;
    var row = table.insertRow(rowCount);
    var cell1 = row.insertCell(0);
    //var element1 = document.createElement("input");
    //element1.type = "checkbox";
    //element1.type = "chkbox[]";
    //cell1.appendChild(element1);
    cell1.innerHTML = "<div class='checkbox'><label><input type='checkbox' value=''></label></div>";
    var cell2 = row.insertCell(1);
    cell2.innerHTML = rowCount + 1;
    var cell3 = row.insertCell(2);
    var tax_class = document.getElementById("classtaxonomy").value;
    cell3.innerHTML = tax_class;
}
function deleteConcept() {
    try {
        var table = document.getElementById("class_table");
        var rowCount = table.rows.length;
        for (var i = 0; i < rowCount; i++) {
            var row = table.rows[i];
            var chkbox = row.cells[0].childNodes[0].childNodes[0].childNodes[0];
            if (null != chkbox && true == chkbox.checked) {
                table.deleteRow(i);
                rowCount--;
                i--;
            }
        }
    } catch (e) {
        alert(e);
    }
}
//output
function addClassificationConceptsToSD(kb) {
    var class_table = document.getElementById("class_table");
    for (i = 0; i < class_table.rows.length; i++) {
        kb.add("sd:" + document.getElementById("sd_model").value + " usdl-core-cb:hasClassificationDimension " + "<" + class_table.rows[i].cells[2].innerHTML + "> .")
    }
    return kb;
}
//output
function addQualServiceLevelsToSD(kb) {
    var qual_sl_table = document.getElementById("qualserviceleveldetails");
    for (i = 1; i < qual_sl_table.rows.length; i++) {
        var qual_value_type = qual_sl_table.rows[i].cells[0].childNodes[0].value;
        // SL instance
        kb.add("sd:SL" + qual_value_type + "Instance a " + "bp:SL" + qual_value_type + " .")
                .add("sd:" + document.getElementById("slp").value + " bp:hasSL" + qual_value_type + " sd:SL" + qual_value_type + "Instance .")
        // SLE instance
        kb.add("sd:SLE" + qual_value_type + "Instance a " + "bp:SLE" + qual_value_type + " .")
                .add("sd:SL" + qual_value_type + "Instance bp:hasSLE" + qual_value_type + " sd:SLE" + qual_value_type + "Instance .")
        // Var instance
        kb.add("sd:Var" + qual_value_type + "Instance a " + "bp:Var" + qual_value_type + " .")
                .add("sd:SLE" + qual_value_type + "Instance bp:hasVar" + qual_value_type + " sd:Var" + qual_value_type + "Instance .")
        // bind var instance to the qual value instance
        var ddlb = document.getElementById("ddlb_" + qual_value_type);
        kb.add("sd:Var" + qual_value_type + "Instance  " + "bp:hasDefault" + qual_value_type + " <" + ddlb.value + "> .");
    }
    return kb;
}
//output
function addQuantServiceLevelsToSD(kb) {
    var prefix = 'quantitativeValues_';
    var quant_sl_table = document.getElementById("slvaluequant");
    var quant_sl_size = document.getElementById("slQuantitative").children.length;
    //for (i = 1; i < quant_sl_table.rows.length; i++) {
    for (i = 1; i <= quant_sl_size; i++) {
        var idType = prefix + i + '_type';
        var idInstance = prefix + i + '_instance';
        var idInstanceDesc = prefix + i + '_instanceDesc';
        var idUom = prefix + i + '_uom';
        var idIsRange = prefix + i + '_isRange';
        var idValueType = prefix + i + '_valueType';
        var idMinValue = prefix + i + '_minValue';
        var idMaxValue = prefix + i + '_maxValue';
        var idValue = prefix + i + '_value';
        var quant_value_typeNG = document.getElementById(idType).innerHTML;
        var quant_value_instNG = document.getElementById(idInstance).value;
        var uomNG = document.getElementById(idUom).value;
        var isRangeNG = document.getElementById(idIsRange).checked;
        var minValueNG = document.getElementById(idMinValue).value;
        var maxValueNG = document.getElementById(idMaxValue).value;
        var valueNG = undefined;
        if (!isRangeNG) {
            valueNG = document.getElementById(idValue).value;
        }
        var valueTypeNG = document.getElementById(idValueType).value;
        var instanceDescriptionNG = document.getElementById(idInstanceDesc).value;
        // SL instance
        kb.add("sd:SL" + quant_value_typeNG + "Instance a " + "bp:SL" + quant_value_typeNG + " .");
        kb.add("sd:" + document.getElementById("slp").value + " bp:hasSL" + quant_value_typeNG + " sd:SL" + quant_value_typeNG + "Instance .");
        // SLE instance
        kb.add("sd:SLE" + quant_value_typeNG + "Instance a " + "bp:SLE" + quant_value_typeNG + " .");
        kb.add("sd:SL" + quant_value_typeNG + "Instance bp:hasSLE" + quant_value_typeNG + " sd:SLE" + quant_value_typeNG + "Instance .");
        // Var instance
        kb.add("sd:Var" + quant_value_typeNG + "Instance a " + "bp:Var" + quant_value_typeNG + " .");
        kb.add("sd:SLE" + quant_value_typeNG + "Instance bp:hasVar" + quant_value_typeNG + " sd:Var" + quant_value_typeNG + "Instance .");
        // bind var instance to the quant value instance
        kb.add("sd:" + quant_value_instNG + " a bp:" + quant_value_typeNG + " .");
        // id = prefix + _i_ + instanceDescription
        kb.add("sd:" + quant_value_instNG + " " + ' rdfs:label "' + instanceDescriptionNG + '"^^xsd:string .');
        // id = prefix + _i_ + uom
        kb.add("sd:" + quant_value_instNG + " " + ' gr:hasUnitOfMeasurement "' + uomNG + '"^^xsd:string .')
        kb.add("sd:" + quant_value_instNG + " " + "gr:valueReference " + "bp:" + quant_value_typeNG + "ValueRange .")
        kb.add("sd:Var" + quant_value_typeNG + "Instance  " + "bp:hasDefault" + quant_value_typeNG + " sd:" + quant_value_instNG + " .");
        // specify quant value instance  Here the parsing code has to be adapted!!!
        if (isRangeNG == true) {
            // int or float value subclass
            // id = prefix + _i_ + valueType
            if (valueTypeNG == "integer") {
                // Int value subclass
                // id = prefix + _i_ + 
                kb.add("sd:" + quant_value_instNG + " " + ' gr:hasMinValueInteger "' + minValueNG + '"^^<http://www.w3.org/2001/XMLSchema#integer> .')
                        .add("sd:" + quant_value_instNG + " " + ' gr:hasMaxValueInteger "' + maxValueNG + '"^^<http://www.w3.org/2001/XMLSchema#integer> .')
            } else {
                // Float value subclass
                kb.add("sd:" + quant_value_instNG + " " + ' gr:hasMinValueFloat "' + minValueNG + '"^^xsd:float .')
                        .add("sd:" + quant_value_instNG + " " + ' gr:hasMaxValueFloat "' + maxValueNG + '"^^xsd:float .')
            }
        } else {
            // int or float value subclass
            if (valueTypeNG == "integer") {
                // Int value subclass
                kb.add("sd:" + quant_value_instNG + " " + ' gr:hasValueInteger "' + valueNG + '"^^<http://www.w3.org/2001/XMLSchema#integer> .')
                        .add("sd:" + quant_value_instNG + " " + ' gr:hasMinValueInteger "' + valueNG + '"^^<http://www.w3.org/2001/XMLSchema#integer> .')
                        .add("sd:" + quant_value_instNG + " " + ' gr:hasMaxValueInteger "' + valueNG + '"^^<http://www.w3.org/2001/XMLSchema#integer> .')
            } else {
                // Float value subclass
                kb.add("sd:" + quant_value_instNG + " " + ' gr:hasValueFloat "' + valueNG + '"^^xsd:float .')
                        .add("sd:" + quant_value_instNG + " " + ' gr:hasMinValueFloat "' + valueNG + '"^^xsd:float .')
                        .add("sd:" + quant_value_instNG + " " + ' gr:hasMaxValueFloat "' + valueNG + '"^^xsd:float .')
            }
        }
    }
    return kb;
}
function loadBP(event) {
    event.preventDefault();
    var dt = event.dataTransfer
    var files = dt.files
    if (files.length == 1) {
        var reader = new FileReader();
        reader.onload = function (event) {
            var content = event.target.result;
            var kb = $.rdf();
            kb.load(content);
            kb.prefix('foaf', 'http://xmlns.com/foaf/0.1/')
                    .prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
                    .prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
                    .prefix('owl', 'http://www.w3.org/2002/07/owl#')
                    .prefix('dcterms', 'http://purl.org/dc/terms/')
                    .prefix('usdl-core', 'http://www.linked-usdl.org/ns/usdl-core#')
                    .prefix('usdl-core-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker#')
                    .prefix('usdl-sla', 'http://www.linked-usdl.org/ns/usdl-sla#')
                    .prefix('usdl-sla-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker-sla#')
                    .prefix('usdl-business-roles', 'http://www.linked-usdl.org/ns/usdl-business-roles#')
                    .prefix('blueprint', 'http://bizweb.sap.com/TR/blueprint#')
                    .prefix('vcard', 'http://www.w3.org/2006/vcard/ns#')
                    .prefix('xsd', 'http://www.w3.org/2001/XMLSchema#')
                    .prefix('ctag', 'http://commontag.org/ns#')
                    .prefix('org', 'http://www.w3.org/ns/org#')
                    .prefix('skos', 'http://www.w3.org/2004/02/skos/core#')
                    .prefix('time', 'http://www.w3.org/2006/time#')
                    .prefix('gr', 'http://purl.org/goodrelations/v1#')
                    .prefix('doap', 'http://usefulinc.com/ns/doap#')
                    .base(findBase(kb) + "#");
            kb.prefix('bp', kb.base());
            loadClassificationTaxonomy(kb);
            loadClassificationConcepts(kb);
            //Load SLP class
            var slpclasses = kb.where("?slps rdfs:subClassOf usdl-sla:ServiceLevelProfile");
            slpclasses.each(function () {
                var sel = this["slps"];
                var url_slps = sel.value._string;
                document.getElementById("slp_class").value = url_slps.split('#')[1];
            });
            //this is obsolete...
            //loadQuantitativeSLSchemas(kb);
            loadQualitativeSLSchemasNG(kb);
            deactivateInputFields();
            //Create quant values table
            createQuantitativeSLValuesTable(kb);
            document.kb = kb;
        }
        reader.readAsText(files[0]);
        document.getElementById("bp").innerHTML = "Broker policy loaded!";
        $("#serviceEditArea").collapse('show');
        $("#serviceLoadArea").collapse('hide');
    }
}
function loadClassifications(kb, url_sdmodel) {
    var classifications = kb.where("<" + url_sdmodel + "> usdl-core-cb:hasClassificationDimension ?classification");
    classifications.each(function () {
        var classification = this["classification"];
        var url_classification = classification.value._string;
        var table = document.getElementById("class_table");
        var rowCount = table.rows.length;
        var row = table.insertRow(rowCount);
        var cell1 = row.insertCell(0);
        cell1.innerHTML = "<div class='checkbox'><label><input type='checkbox' value=''></label></div>";
        var cell2 = row.insertCell(1);
        cell2.innerHTML = rowCount + 1;
        var cell3 = row.insertCell(2);
        var tax_class = document.getElementById("classtaxonomy").value;
        cell3.innerHTML = url_classification;
    })
}
function findSDbase(kb) {
    var base = kb.databank.baseURI
    var services = kb.where("?sd a usdl-core:Service")
            .where("?sd dcterms:title ?title")
            .where("?sd dcterms:description ?description")
            .where("?sd usdl-core-cb:validFrom ?validFrom");
    services.each(function () {
        var selection_sd = this["sd"];
        var selection_title = this["title"];
        var selection_description = this["description"];
        var validFrom = this["validFrom"];
        var url_sd = selection_sd.value._string;
        base = url_sd.split('#')[0];
        document.getElementById("sd_namespace").value = base;
        var url_title = selection_title.value._string;
        var url_description = selection_description.value._string;
        // Assign the service instance to the sd form
        document.getElementById("sd").value = url_sd.split('#')[1];
        document.getElementById("sd_title").value = selection_title.value;
        document.getElementById("sd_description").value = selection_description.value;
        document.getElementById("valid_from_sd").value = validFrom.value;
    })
    var validthrough = kb.where("?sd a usdl-core:Service")
            .where("?sd usdl-core-cb:validThrough ?validThrough");
    document.getElementById("valid_through_sd").value = "";
    validthrough.each(function () {
        var validThrough = this["validThrough"];
        document.getElementById("valid_through_sd").value = validThrough.value;
    })
    var successor = kb.where("?sd a usdl-core:Service")
            .where("?sd gr:successorOf ?successor");
    successor.each(function () {
        var successor_of = this["successor"];
        document.getElementById("successor_of_sd").value = successor_of.value._string;
    })
    var recommendation = kb.where("?sd a usdl-core:Service")
            .where("?sd gr:successorOf ?successor")
            .where("?sd usdl-core-cb:deprecationRecommendationTimePointSD ?recommendation");
    document.getElementById("deprecation_recommendation_sd").value = "";
    recommendation.each(function () {
        var recommendation_date = this["recommendation"];
        document.getElementById("deprecation_recommendation_sd").value = recommendation_date.value;
    })
    var models = kb.where("?sdModel a <" + document.getElementById("bp_namespace").value + "#" + document.getElementById("bp_model").value + ">")
            .where(" ?sdModel <" + document.getElementById("bp_namespace").value + "#has" + document.getElementById("slp_class").value + "> ?profile");
    models.each(function () {
        var selection_sdModel = this["sdModel"];
        var selection_profile = this["profile"];
        var url_sdModel = selection_sdModel.value._string;
        var url_profile = selection_profile.value._string;
        // Assign the service model instance to the sd form
        document.getElementById("sd_model").value = url_sdModel.split('#')[1];
        document.getElementById("slp").value = url_profile.split('#')[1];
        loadClassifications(kb, url_sdModel);
    })
    var business_entity = kb.where("?bentity a gr:BusinessEntity")
            .where("?bentity gr:legalName ?legalName")
    business_entity.each(function () {
        var bizentity = this["bentity"];
        var legalname = this["legalName"];
        var url_bizentity = bizentity.value._string;
        // Assign the service model class and instance to the business policy form
        document.getElementById("sp_namespace").value = url_bizentity.split('#')[0];
        document.getElementById("sp_business_entity").value = url_bizentity.split('#')[1];
        document.getElementById("sp_legal_name").value = legalname.value;
    })
    return base
}
function loadQualSDSLs(kb) {
    var qual_sl_table = document.getElementById("qualserviceleveldetails");
    for (i = 1; i < qual_sl_table.rows.length; i++) {
        var qual_value_type = qual_sl_table.rows[i].cells[0].childNodes[0].value;
        var qual_value = kb.where("sd:Var" + qual_value_type + "Instance " + "bp:hasDefault" + qual_value_type + " ?qualvalue")
        qual_value.each(function () {
            var qvalue = this["qualvalue"];
            var qvalue_url = qvalue.value._string;
            var ddlb = document.getElementById("ddlb_" + qual_value_type);
            for (j = 0; j < ddlb.options.length; j++) {
                if (ddlb.options[j].value == qvalue_url) {
                    ddlb.options[j].selected = "selected";
                }
            }
        })
    }
}
function loadQuantSDSLs(kb) {
    var prefix = 'quantitativeValues_';
    var quant_sl_table = document.getElementById("slvaluequant");
    var quant_sl_size = document.getElementById("slQuantitative").children.length;
    //for (i = 1; i < quant_sl_table.rows.length; i++) {
    for (i = 1; i <= quant_sl_size; i++) {
        var idType = prefix + i + '_type';
        var idInstance = prefix + i + '_instance';
        var idInstanceDesc = prefix + i + '_instanceDesc';
        var idIsRange = prefix + i + '_isRange';
        var idValueType = prefix + i + '_valueType';
        var idMinValue = prefix + i + '_minValue';
        var idMaxValue = prefix + i + '_maxValue';
        var idValue = prefix + i + '_value';
        var quant_value_typeNG = document.getElementById(idType).innerHTML;
        var quant_value_instNG = document.getElementById(idInstance).value;
        var isRangeNG = document.getElementById(idIsRange).checked;
        var valueNG = undefined;
        if (!isRangeNG) {
            valueNG = document.getElementById(idValue).value;
        }
        var valueTypeNG = document.getElementById(idValueType).value;
        if (isRangeNG == true) {
            // int or float value range subclass
            // id = prefix + _i_ + valueType
            if (valueTypeNG == "integer") {
                // Int value range subclass
                // Query instance from the kb
                var quant_value = kb.where("?quantvalue" + " a bp:" + quant_value_typeNG)
                        .where("?quantvalue" + " rdfs:label ?label")
                        .where("?quantvalue" + " gr:hasMinValueInteger ?min")
                        .where("?quantvalue" + " gr:hasMaxValueInteger ?max");
                quant_value.each(function () {
                    var qvalue = this["quantvalue"];
                    var qvalue_url = qvalue.value._string;
                    var desc = this["label"];
                    var minval = this["min"];
                    var maxval = this["max"];
                    document.getElementById(idInstance).value = qvalue_url.split("#")[1];
                    document.getElementById(idInstanceDesc).value = desc.value;
                    document.getElementById(idMinValue).value = minval.value;
                    document.getElementById(idMaxValue).value = maxval.value;
                })
            } else {
                // Float value range subclass
                var quant_value = kb.where("?quantvalue" + " a bp:" + quant_value_typeNG)
                        .where("?quantvalue" + " rdfs:label ?label")
                        .where("?quantvalue" + " gr:hasMinValueFloat ?min")
                        .where("?quantvalue" + " gr:hasMaxValueFloat ?max");
                quant_value.each(function () {
                    var qvalue = this["quantvalue"];
                    var qvalue_url = qvalue.value._string;
                    var desc = this["label"];
                    var minval = this["min"];
                    var maxval = this["max"];
                    document.getElementById(idInstance).value = qvalue_url.split("#")[1];
                    document.getElementById(idInstanceDesc).value = desc.value;
                    document.getElementById(idMinValue).value = minval.value;
                    document.getElementById(idMaxValue).value = maxval.value;
                })
            }
        } else {
            // int or float value subclass
            if (valueTypeNG == "integer") {
                // Int value subclass
                var quant_value = kb.where("?quantvalue" + " a bp:" + quant_value_typeNG)
                        .where("?quantvalue" + " rdfs:label ?label")
                        .where("?quantvalue" + " gr:hasValueInteger ?val");
                quant_value.each(function () {
                    var qvalue = this["quantvalue"];
                    var qvalue_url = qvalue.value._string;
                    var desc = this["label"];
                    var intval = this["val"];
                    document.getElementById(idInstance).value = qvalue_url.split("#")[1];
                    document.getElementById(idInstanceDesc).value = desc.value;
                    document.getElementById(idValue).value = intval.value;
                })
            } else {
                // Float value subclass
                var quant_value = kb.where("?quantvalue" + " a bp:" + quant_value_typeNG)
                        .where("?quantvalue" + " rdfs:label ?label")
                        .where("?quantvalue" + " gr:hasValueFloat ?val");
                quant_value.each(function () {
                    var qvalue = this["quantvalue"];
                    var qvalue_url = qvalue.value._string;
                    var desc = this["label"];
                    var floatval = this["val"];
                    document.getElementById(idInstance).value = qvalue_url.split("#")[1];
                    document.getElementById(idInstanceDesc).value = desc.value;
                    document.getElementById(idValue).value = floatval.value;
                })
            }
        }
    }
}
function loadSD(event) {
    event.preventDefault();
    var dt = event.dataTransfer
    var files = dt.files
    if (files.length == 1) {
        var reader = new FileReader();
        reader.onload = function (event) {
            var content = event.target.result;
            var kbsd = $.rdf();
            kbsd.load(content);
            kbsd.prefix('foaf', 'http://xmlns.com/foaf/0.1/')
                    .prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
                    .prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
                    .prefix('owl', 'http://www.w3.org/2002/07/owl#')
                    .prefix('dcterms', 'http://purl.org/dc/terms/')
                    .prefix('usdl-core', 'http://www.linked-usdl.org/ns/usdl-core#')
                    .prefix('usdl-core-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker#')
                    .prefix('usdl-sla', 'http://www.linked-usdl.org/ns/usdl-sla#')
                    .prefix('usdl-sla-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker-sla#')
                    .prefix('usdl-business-roles', 'http://www.linked-usdl.org/ns/usdl-business-roles#')
                    .prefix('blueprint', 'http://bizweb.sap.com/TR/blueprint#')
                    .prefix('vcard', 'http://www.w3.org/2006/vcard/ns#')
                    .prefix('xsd', 'http://www.w3.org/2001/XMLSchema#')
                    .prefix('ctag', 'http://commontag.org/ns#')
                    .prefix('org', 'http://www.w3.org/ns/org#')
                    .prefix('skos', 'http://www.w3.org/2004/02/skos/core#')
                    .prefix('time', 'http://www.w3.org/2006/time#')
                    .prefix('gr', 'http://purl.org/goodrelations/v1#')
                    .prefix('doap', 'http://usefulinc.com/ns/doap#')
                    .prefix('bp', document.getElementById("bp_namespace").value + "#")
                    .base(findSDbase(kbsd) + "#");
            kbsd.prefix('sd', document.getElementById("sd_namespace").value + "#")
                    .prefix('sp', document.getElementById("sp_namespace").value + "#");
            loadClassifications(kbsd);
            loadQualSDSLs(kbsd);
            loadQuantSDSLs(kbsd);
            document.kbsd = kbsd;
        }
        reader.readAsText(files[0]);
        document.getElementById("sd_load").innerHTML = "Service description loaded!";
        $("#serviceEditArea").collapse('show');
        $("#serviceDescriptionLoadArea").collapse('hide');
    }
}
function loadQualValuesNG(kb, serviceLevel, description) {
    //create table
    var qvTable = document.createElement('table');
    qvTable.setAttribute('id', 'table_' + serviceLevel);
    qvTable.setAttribute('style', 'width: 90%');
    var qvHeader = qvTable.insertRow();
    [{
            content: 'Value',
            class: 'col-sm-3'
        }, {
            content: 'Description',
            class: 'col-sm-9'
        }].forEach(function (descriptor) {
        var headerCell = document.createElement('th');
        if (descriptor.content)
            headerCell.appendChild(document.createTextNode(descriptor.content));
        if (descriptor.class)
            headerCell.setAttribute('class', descriptor.class);
        qvHeader.appendChild(headerCell);
    });
    var selectbox = document.createElement("SELECT");
    selectbox.setAttribute('id', 'ddlb_' + serviceLevel);
    // fill table with data
    var query = kb.where("?valinst a bp:" + serviceLevel + "");
    var rowcounter = 0;
    query.each(function () {
        var qvRow = qvTable.insertRow();
        var selection = this["valinst"];
        var qv_url = selection.value._string;
        var sl = document.createElement('input');
        sl.setAttribute('type', 'text');
        sl.setAttribute('value', qv_url.split("#")[1]);
        sl.setAttribute('disabled', 'true');
        qvRow.insertCell().appendChild(sl);
        var cell4Value = '';
        var label = kb.where("<" + qv_url + ">" + " rdfs:label ?label");
        label.each(function () {
            cell4Value = this["label"].value;
        });
        var slDesc = document.createElement('input');
        slDesc.setAttribute('type', 'text');
        slDesc.setAttribute('value', cell4Value);
        slDesc.setAttribute('disabled', 'true');
        qvRow.insertCell().appendChild(slDesc);
        var selectOption = document.createElement('option');
        selectOption.setAttribute('value', qv_url);
        selectOption.appendChild(document.createTextNode(qv_url.split("#")[1]));
        selectbox.appendChild(selectOption);
    })
    var panel = document.createElement('div');
    panel.className = 'panel panel-default';
    var panelHeader = document.createElement('div');
    panelHeader.className = 'panel-heading';
    //var qvDiv = document.createElement("div");
    var header = document.createElement('H2');
    header.appendChild(document.createTextNode('Value set of ' + serviceLevel));
    panelHeader.appendChild(header);
    var panelBody = document.createElement('div');
    panelBody.classNAme = 'panel-body';
    var descriptionNode = document.createElement('p');
    descriptionNode.appendChild(document.createTextNode(description));
    panelBody.appendChild(descriptionNode);
    panelBody.appendChild(qvTable);
    panelBody.appendChild(selectbox);
    panel.appendChild(panelHeader);
    panel.appendChild(panelBody);
    document.getElementById("qualvalues").appendChild(panel);
}
function loadQualitativeSLSchemasNG(kb) {
    var table = document.getElementById("qualserviceleveldetails");
    var quant_sls = kb.where("?qvc rdfs:subClassOf gr:QualitativeValue");
    quant_sls.each(function () {
        var selection = this["qvc"];
        var sls_url = selection.value._string;
        var serviceLevel = sls_url.split("#")[1];
        var serviceLevelDescription = '';
        var label = kb.where("<" + sls_url + ">" + " rdfs:label ?label");
        label.each(function () {
            serviceLevelDescription = this["label"].value;
        });
        var rowCount = table.rows.length;
        var row = table.insertRow(rowCount);
        var cell3 = row.insertCell();
        var serviceLevelInput = document.createElement("INPUT");
        serviceLevelInput.setAttribute('type', 'text');
        serviceLevelInput.setAttribute('value', serviceLevel);
        cell3.appendChild(serviceLevelInput);
        var cell4 = row.insertCell();
        var serviceLevelDescriptionInput = document.createElement("INPUT");
        serviceLevelDescriptionInput.setAttribute('type', 'text');
        serviceLevelDescriptionInput.setAttribute('value', serviceLevelDescription);
        cell4.appendChild(serviceLevelDescriptionInput);
        loadQualValuesNG(kb, serviceLevel, serviceLevelDescription);
    });
}
function createQuantitativeValueForm(qv, prefix) {
    var createTextBox = function (title, content, id) {
        var div = document.createElement('div');
        div.setAttribute('class', 'form-group');
        div.appendChild(function () {
            var label = document.createElement('label');
            label.htmlFor = id;
            label.appendChild(document.createTextNode(title));
            return label;
        });
        div.appendChild(function () {
            var input = document.createElement('p');
            input.id = id;
            input.className('form-control-static');
            input.appendChild(document.createTextNode(content));
            return input;
        });
        return div;
    };
    var createTextInputBox = function (title, content, id, readonly, descriptorSize, contentSize) {
        var div = document.createElement('div');
        div.setAttribute('class', 'form-group');
        div.appendChild(function () {
            var label = document.createElement('label');
            label.htmlFor = id;
            label.className = 'col-sm-' + descriptorSize + ' control-label';
            label.appendChild(document.createTextNode(title));
            return label;
        }());
        div.appendChild(function () {
            var innerDiv = document.createElement('div');
            innerDiv.className = 'col-sm-' + contentSize;
            innerDiv.appendChild(function () {
                var input = document.createElement('input');
                input.type = 'text';
                input.id = id;
                input.value = content;
                input.className = 'form-control';
                if (readonly)
                    input.disabled = true;
                return input;
            }());
            return innerDiv;
        }());
        return div;
    };
    var createTextAreaBox = function (title, content, id, readonly, descriptorSize, contentSize) {
        var div = document.createElement('div');
        div.setAttribute('class', 'form-group');
        div.appendChild(function () {
            var label = document.createElement('label');
            label.htmlFor = id;
            label.className = 'col-sm-' + descriptorSize + ' control-label';
            label.appendChild(document.createTextNode(title));
            return label;
        }());
        div.appendChild(function () {
            var innerDiv = document.createElement('div');
            innerDiv.className = 'col-sm-10';
            innerDiv.appendChild(function () {
                var input = document.createElement('textarea');
                input.rows = 5;
                input.id = id;
                input.value = content;
                input.className = 'form-control col-sm-' + contentSize;
                if (readonly)
                    input.disabled = true;
                return input;
            }());
            return innerDiv;
        }());
        return div;
    };
    var createCheckBox = function (title, check, id, descriptorSize, contentSize) {
        var div = document.createElement('div');
        div.setAttribute('class', 'checkbox');
        div.appendChild(function () {
            var label = document.createElement('label');
            var checkbox = document.createElement('input');
            checkbox.setAttribute("type", "checkbox");
            checkbox.id = id;
            checkbox.checked = check;
            checkbox.disabled = true;
            label.appendChild(checkbox);
            label.appendChild(document.createTextNode(title));
            return label;
        }());
        var outerDiv = document.createElement('div');
        outerDiv.className = 'col-sm-offset-' + descriptorSize + ' col-sm-' + contentSize;
        outerDiv.appendChild(div);
        return outerDiv;
    };
    var panel = document.createElement('div');
    panel.className = "panel panel-default panel2col";
    panel.appendChild(function () {
        var panelHeading = document.createElement('div');
        panelHeading.className = 'panel-heading';
        panelHeading.appendChild(function () {
            var header = document.createElement('h2');
            header.appendChild(document.createTextNode(qv.type));
            header.id = prefix + '_type';
            return header;
        }());
        return panelHeading;
    }());
    var form = document.createElement('form');
    form.setAttribute('class', 'form-horizontal');
    form.setAttribute('id', prefix + 'form');
    form.appendChild(function () {
        var paragraph = document.createElement('p');
        paragraph.appendChild(document.createTextNode(qv.description));
        return paragraph;
    }());
    form.appendChild(
            createTextInputBox('Service Level', '', prefix + '_instance', false, 2, 10)
            );
    form.appendChild(
            createTextAreaBox('Service Level Description', '', prefix + '_instanceDesc', false, 2, 10)
            );
    form.appendChild(
            createTextInputBox('Unit of Measurement', qv.uom, prefix + '_uom', true, 2, 10)
            );
    form.appendChild(
            createTextInputBox('Value type', qv.valueType, prefix + '_valueType', true, 2, 10)
            );
    if (!qv.isRange) {
        form.appendChild(
                createTextInputBox('Min Value', qv.minValue, prefix + '_minValue', true, 2, 10)
                );
        form.appendChild(
                createTextInputBox('Value', '', prefix + '_value', false, 2, 10)
                );
        form.appendChild(
                createTextInputBox('Max Value', qv.maxValue, prefix + '_maxValue', true, 2, 10)
                );
    } else {
        form.appendChild(
                createTextInputBox('Min Value', qv.minValue, prefix + '_minValue', false, 2, 10)
                );
        form.appendChild(
                createTextInputBox('Max Value', qv.maxValue, prefix + '_maxValue', false, 2, 10)
                );
    }
    form.appendChild(createCheckBox('Is Range', qv.isRange, prefix + '_isRange', 2, 4));
    form.appendChild(createCheckBox('Higher is Better', qv.higherIsBetter, prefix + '_higherIsBetter', 2, 4));
    panel.appendChild(function () {
        var panelBody = document.createElement('div');
        panelBody.className = 'panel-body';
        panelBody.appendChild(form);
        return panelBody;
    }());
    return panel;
}
function createQuantitativeValueEntry(selection, kb, i, prefix, valueType) {
    var idType = prefix + i + '_type'; // 2
    var idInstance = prefix + i + '_instance'; // 4
    var idInstanceDesc = prefix + i + '_instanceDesc'; // 5
    var idUom = prefix + i + '_uom'; // 6
    var idIsRange = prefix + i + 'i_sRange'; // 11
    var idValueType = prefix + i + '_valueType'; // 7
    var idMinValue = prefix + i + '_minValue'; // 8
    var idMaxValue = prefix + i + '_maxValue'; // 10
    var idValue = prefix + i + '_value'; // 9
    var sls_url = selection.value._string;
    var qv = {};
    qv.type = sls_url.split("#")[1];
    //Label
    var label = kb.where("<" + sls_url + ">" + " rdfs:label ?label");
    label.each(function () {
        qv.description = this["label"].value;
    });
    var uom = kb.where("<" + sls_url + ">" + " gr:hasUnitOfMeasurement ?uom");
    uom.each(function () {
        qv.uom = this["uom"].value;
    });
    qv.valueType = valueType;
    var minv;
    if (valueType === 'integer') {
        minv = kb.where("<" + sls_url + ">" + " gr:hasMinValueInteger ?minv");
    } else {
        minv = kb.where("<" + sls_url + ">" + " gr:hasMinValueFloat ?minv");
    }
    minv.each(function () {
        qv.minValue = this["minv"].value;
    });
    var maxv;
    if (valueType === 'integer') {
        maxv = kb.where("<" + sls_url + ">" + " gr:hasMaxValueInteger ?maxv");
    } else {
        maxv = kb.where("<" + sls_url + ">" + " gr:hasMaxValueFloat ?maxv");
    }
    maxv.each(function () {
        qv.maxValue = this["maxv"].value;
    });
    var range = kb.where("<" + sls_url + ">" + " usdl-core-cb:isRange ?range");
    range.each(function () {
        qv.isRange = this["range"].value;
    });
    var hib = kb.where("<" + sls_url + ">" + " usdl-core-cb:higherIsBetter ?hib");
    hib.each(function () {
        qv.higherIsBetter = this["hib"].value;
    });
    document.getElementById("slQuantitative").appendChild(createQuantitativeValueForm(qv, prefix + i));
}
function createQuantitativeSLValuesTable(kb) {
    // Create integer values
    var quant_sls = kb.where("?qvc rdfs:subClassOf gr:QuantitativeValueInteger");
    var prefix = 'quantitativeValues_';
    var i = 0;
    quant_sls.each(function () {
        i++;
        createQuantitativeValueEntry(this["qvc"], kb, i, prefix, "integer");
    });
    // Create float values
    quant_sls = kb.where("?qvc rdfs:subClassOf gr:QuantitativeValueFloat");
    quant_sls.each(function () {
        i++;
        createQuantitativeValueEntry(this["qvc"], kb, i, prefix, "float");
    });
}
function loadClassificationTaxonomy(kb) {
    // load top concept
    var topconcept = kb.where("?concept a usdl-core-cb:ClassificationDimension")
            .where("?scheme a skos:ConceptScheme")
            .where("?concept skos:topConceptOf ?scheme");
    var tc = "";
    topconcept.each(function () {
        var selection = this["concept"];
        var tc_url = selection.value._string;
        tc = tc_url;
        document.getElementById("classtaxURI").value = tc_url.split("#")[0];
        document.getElementById("classtaxroot").valueclasstaxroot = tc_url.split("#")[1];
    })
    var query = "?classconcept skos:broader <" + tc + ">";
    var classconcepts = kb.where(query)
            .where("?classconcept a usdl-core-cb:ClassificationDimension");
    var c_url = "";
    classconcepts.each(function () {
        var selection = this["classconcept"];
        var c_url = selection.value._string;
        var table = document.getElementById("classtaxconcepts");
        var row = table.insertRow();
        var cell3 = row.insertCell();
        cell3.innerHTML = "<input type='text'>";
        cell3.childNodes[0].value = c_url.split("#")[1];
        var cell4 = row.insertCell();
        cell4.innerHTML = "<input type='text'>";
        var cell5 = row.insertCell();
        cell5.innerHTML = "<input type='text'>";
        var title = kb.where("<" + c_url + ">" + " dcterms:title ?title");
        title.each(function () {
            cell4.childNodes[0].value = this["title"].value;
        })
        var label = kb.where("<" + c_url + ">" + " skos:prefLabel ?label");
        label.each(function () {
            cell5.childNodes[0].value = this["label"].value;
        })
    })
}
function loadClassificationConcepts(kb) {
    var classconcepts = kb.where("?concept a usdl-core-cb:ClassificationDimension");
    classconcepts.each(function () {
        var selection = this["concept"];
        var url = selection.value._string;
        var selectelement = document.getElementById("classtaxonomy");
        var concept = document.createElement("option");
        concept.value = url;
        concept.text = url.split('#')[1];
        selectelement.add(concept, null);
    })
}
function storeSD() {
    var sd_namespace = document.getElementById("sd_namespace").value;
    var sp_namespace = document.getElementById("sp_namespace").value;
    var kb_sd = $.rdf()
            .base(sd_namespace)
            .prefix('foaf', 'http://xmlns.com/foaf/0.1/')
            .prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
            .prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
            .prefix('owl', 'http://www.w3.org/2002/07/owl#')
            .prefix('dcterms', 'http://purl.org/dc/terms/')
            .prefix('usdl-core', 'http://www.linked-usdl.org/ns/usdl-core#')
            .prefix('usdl-core-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker#')
            .prefix('usdl-sla', 'http://www.linked-usdl.org/ns/usdl-sla#')
            .prefix('usdl-sla-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker-sla#')
            .prefix('usdl-business-roles', 'http://www.linked-usdl.org/ns/usdl-business-roles#')
            .prefix('blueprint', 'http://bizweb.sap.com/TR/blueprint#')
            .prefix('vcard', 'http://www.w3.org/2006/vcard/ns#')
            .prefix('xsd', 'http://www.w3.org/2001/XMLSchema#')
            .prefix('ctag', 'http://commontag.org/ns#')
            .prefix('org', 'http://www.w3.org/ns/org#')
            .prefix('skos', 'http://www.w3.org/2004/02/skos/core#')
            .prefix('time', 'http://www.w3.org/2006/time#')
            .prefix('gr', 'http://purl.org/goodrelations/v1#')
            .prefix('doap', 'http://usefulinc.com/ns/doap#')
            .prefix('bp', document.kb.databank.baseURI)
            .prefix('sd', sd_namespace + "#")
            .prefix('sp', sp_namespace + "#")
            // Create the business entity of the service provider
            .add("sp:" + document.getElementById("sp_business_entity").value + " a gr:BusinessEntity .")
            .add('sp:' + document.getElementById("sp_business_entity").value + ' gr:legalName ' + '"' + document.getElementById("sp_legal_name").value + '"' + ' .')
            // Create the entity involvement instance and bind it to the service provider business entity
            .add("sd:" + document.getElementById("sp_business_entity").value + "EntityInvolvement" + " a usdl-core:EntityInvolvement .")
            .add("sd:" + document.getElementById("sp_business_entity").value + "EntityInvolvement" + " usdl-core:withBusinessRole usdl-business-roles:provider .")
            .add("sd:" + document.getElementById("sp_business_entity").value + "EntityInvolvement" + " usdl-core:ofBusinessEntity sp:" + document.getElementById("sp_business_entity").value + " .")
            // Create the service instance
            .add("sd:" + document.getElementById("sd").value + " a usdl-core:Service .")
            .add("sd:" + document.getElementById("sd").value + " dcterms:creator sp:" + document.getElementById("sp_business_entity").value + " .")
            .add("sd:" + document.getElementById("sd").value + ' dcterms:title ' + '"' + document.getElementById("sd_title").value + '"' + ' .')
            .add("sd:" + document.getElementById("sd").value + ' dcterms:description ' + '"' + document.getElementById("sd_description").value + '"' + ' .')
            .add("sd:" + document.getElementById("sd").value + " usdl-core:hasEntityInvolvement sd:" + document.getElementById("sp_business_entity").value + "EntityInvolvement .")
            .add("sd:" + document.getElementById("sd").value + " usdl-core-cb:hasServiceModel sd:" + document.getElementById("sd_model").value + " .")
            // Create the service model instance
            .add("sd:" + document.getElementById("sd_model").value + " a bp:" + document.getElementById("bp_model").value + " .")
            .add("sd:" + document.getElementById("sd_model").value + " gr:isVariantOf bp:" + document.getElementById("bp_instance").value + " .")
            // Create and assign a profile instance to the service model
            .add("sd:" + document.getElementById("slp").value + " a bp:" + document.getElementById("slp_class").value + " .")
            .add("sd:" + document.getElementById("sd_model").value + " bp:has" + document.getElementById("slp_class").value + " sd:" + document.getElementById("slp").value + " .");
    //Create validFrom and validThrough
    kb_sd.add("sd:" + document.getElementById("sd").value + ' usdl-core-cb:validFrom ' + '"' + document.getElementById("valid_from_sd").value + '"^^xsd:date .');
    if (document.getElementById("valid_through_sd").value != "") {
        kb_sd.add("sd:" + document.getElementById("sd").value + ' usdl-core-cb:validThrough ' + '"' + document.getElementById("valid_through_sd").value + '"^^xsd:date .');
    }
    // Create successor of and recommendation deprecation, if any.
    // Deprecation recommendation will not be created if there is no successor of even if a value is provided!
    if (document.getElementById("successor_of_sd").value != "") {
        kb_sd.add("sd:" + document.getElementById("sd").value + " gr:successorOf <" + document.getElementById("successor_of_sd").value + "> .");
        if (document.getElementById("deprecation_recommendation_sd").value != "") {
            kb_sd.add("sd:" + document.getElementById("sd").value + ' usdl-core-cb:deprecationRecommendationTimePointSD ' + '"' + document.getElementById("deprecation_recommendation_sd").value + '"^^xsd:date .');
        }
    }
    // Add the chosen classification concepts to the service model
    kb_sd = addClassificationConceptsToSD(kb_sd);
    kb_sd = addQualServiceLevelsToSD(kb_sd);
    kb_sd = addQuantServiceLevelsToSD(kb_sd);
    // Dump the kb with the service description to a turtle format and show it in a separate window.
    var dmp = kb_sd.databank.dump({
        format: 'text/turtle',
        indent: true,
        serialize: true
    })
    var uueFile = Base64.encode(dmp)
    var uri = 'data:text/turtle;base64,' + encodeURIComponent(uueFile)
    window.open(uri)
}

//
//Broker@Clound API
//





function customLoadBP(contentBase64) {

    console.log("Loading Broker Description Service..");
    var content = Base64.decode(contentBase64);
    var kb = $.rdf();
    kb.load(content);
    kb.prefix('foaf', 'http://xmlns.com/foaf/0.1/')
            .prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
            .prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
            .prefix('owl', 'http://www.w3.org/2002/07/owl#')
            .prefix('dcterms', 'http://purl.org/dc/terms/')
            .prefix('usdl-core', 'http://www.linked-usdl.org/ns/usdl-core#')
            .prefix('usdl-core-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker#')
            .prefix('usdl-sla', 'http://www.linked-usdl.org/ns/usdl-sla#')
            .prefix('usdl-sla-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker-sla#')
            .prefix('usdl-business-roles', 'http://www.linked-usdl.org/ns/usdl-business-roles#')
            .prefix('blueprint', 'http://bizweb.sap.com/TR/blueprint#')
            .prefix('vcard', 'http://www.w3.org/2006/vcard/ns#')
            .prefix('xsd', 'http://www.w3.org/2001/XMLSchema#')
            .prefix('ctag', 'http://commontag.org/ns#')
            .prefix('org', 'http://www.w3.org/ns/org#')
            .prefix('skos', 'http://www.w3.org/2004/02/skos/core#')
            .prefix('time', 'http://www.w3.org/2006/time#')
            .prefix('gr', 'http://purl.org/goodrelations/v1#')
            .prefix('doap', 'http://usefulinc.com/ns/doap#')
            .base(findBase(kb) + "#");
    kb.prefix('bp', kb.base());
    loadClassificationTaxonomy(kb);
    loadClassificationConcepts(kb);
    //Load SLP class
    var slpclasses = kb.where("?slps rdfs:subClassOf usdl-sla:ServiceLevelProfile");
    slpclasses.each(function () {
        var sel = this["slps"];
        var url_slps = sel.value._string;
        document.getElementById("slp_class").value = url_slps.split('#')[1];
    });
    loadQualitativeSLSchemasNG(kb);
    deactivateInputFields();
    //Create quant values table
    createQuantitativeSLValuesTable(kb);
    document.kb = kb;

    document.getElementById("bp").innerHTML = "Broker policy loaded!";
    $("#serviceEditArea").collapse('show');
    $("#serviceLoadArea").collapse('hide');
    console.log("Broker Policy loaded...");
}








function customLoadSD(contentBase64) {
    console.log("Loading Broker Description Service..");
    var content = Base64.decode(contentBase64);
    var kbsd = $.rdf();
    kbsd.load(content);

    kbsd.prefix('foaf', 'http://xmlns.com/foaf/0.1/')
            .prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
            .prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
            .prefix('owl', 'http://www.w3.org/2002/07/owl#')
            .prefix('dcterms', 'http://purl.org/dc/terms/')
            .prefix('usdl-core', 'http://www.linked-usdl.org/ns/usdl-core#')
            .prefix('usdl-core-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker#')
            .prefix('usdl-sla', 'http://www.linked-usdl.org/ns/usdl-sla#')
            .prefix('usdl-sla-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker-sla#')
            .prefix('usdl-business-roles', 'http://www.linked-usdl.org/ns/usdl-business-roles#')
            .prefix('blueprint', 'http://bizweb.sap.com/TR/blueprint#')
            .prefix('vcard', 'http://www.w3.org/2006/vcard/ns#')
            .prefix('xsd', 'http://www.w3.org/2001/XMLSchema#')
            .prefix('ctag', 'http://commontag.org/ns#')
            .prefix('org', 'http://www.w3.org/ns/org#')
            .prefix('skos', 'http://www.w3.org/2004/02/skos/core#')
            .prefix('time', 'http://www.w3.org/2006/time#')
            .prefix('gr', 'http://purl.org/goodrelations/v1#')
            .prefix('doap', 'http://usefulinc.com/ns/doap#')
            .prefix('bp', document.getElementById("bp_namespace").value + "#")
            .base(findSDbase(kbsd) + "#");

    kbsd.prefix('sd', document.getElementById("sd_namespace").value + "#")
            .prefix('sp', document.getElementById("sp_namespace").value + "#");

    loadClassifications(kbsd);
    loadQualSDSLs(kbsd);
    loadQuantSDSLs(kbsd);

    document.kbsd = kbsd;

    $("#serviceEditArea").collapse('show');
    console.log("Service Description loaded...");
}


function getCurrentSD() {
    var sd_namespace = document.getElementById("sd_namespace").value;
    var sp_namespace = document.getElementById("sp_namespace").value;
    var kb_sd = $.rdf()
            .base(sd_namespace)
            .prefix('foaf', 'http://xmlns.com/foaf/0.1/')
            .prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
            .prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
            .prefix('owl', 'http://www.w3.org/2002/07/owl#')
            .prefix('dcterms', 'http://purl.org/dc/terms/')
            .prefix('usdl-core', 'http://www.linked-usdl.org/ns/usdl-core#')
            .prefix('usdl-core-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker#')
            .prefix('usdl-sla', 'http://www.linked-usdl.org/ns/usdl-sla#')
            .prefix('usdl-sla-cb', 'http://www.linked-usdl.org/ns/usdl-core/cloud-broker-sla#')
            .prefix('usdl-business-roles', 'http://www.linked-usdl.org/ns/usdl-business-roles#')
            .prefix('blueprint', 'http://bizweb.sap.com/TR/blueprint#')
            .prefix('vcard', 'http://www.w3.org/2006/vcard/ns#')
            .prefix('xsd', 'http://www.w3.org/2001/XMLSchema#')
            .prefix('ctag', 'http://commontag.org/ns#')
            .prefix('org', 'http://www.w3.org/ns/org#')
            .prefix('skos', 'http://www.w3.org/2004/02/skos/core#')
            .prefix('time', 'http://www.w3.org/2006/time#')
            .prefix('gr', 'http://purl.org/goodrelations/v1#')
            .prefix('doap', 'http://usefulinc.com/ns/doap#')
            .prefix('bp', document.kb.databank.baseURI)
            .prefix('sd', sd_namespace + "#")
            .prefix('sp', sp_namespace + "#")
            // Create the business entity of the service provider
            .add("sp:" + document.getElementById("sp_business_entity").value + " a gr:BusinessEntity .")
            .add('sp:' + document.getElementById("sp_business_entity").value + ' gr:legalName ' + '"' + document.getElementById("sp_legal_name").value + '"' + ' .')
            // Create the entity involvement instance and bind it to the service provider business entity
            .add("sd:" + document.getElementById("sp_business_entity").value + "EntityInvolvement" + " a usdl-core:EntityInvolvement .")
            .add("sd:" + document.getElementById("sp_business_entity").value + "EntityInvolvement" + " usdl-core:withBusinessRole usdl-business-roles:provider .")
            .add("sd:" + document.getElementById("sp_business_entity").value + "EntityInvolvement" + " usdl-core:ofBusinessEntity sp:" + document.getElementById("sp_business_entity").value + " .")
            // Create the service instance
            .add("sd:" + document.getElementById("sd").value + " a usdl-core:Service .")
            .add("sd:" + document.getElementById("sd").value + " dcterms:creator sp:" + document.getElementById("sp_business_entity").value + " .")
            .add("sd:" + document.getElementById("sd").value + ' dcterms:title ' + '"' + document.getElementById("sd_title").value + '"' + ' .')
            .add("sd:" + document.getElementById("sd").value + ' dcterms:description ' + '"' + document.getElementById("sd_description").value + '"' + ' .')
            .add("sd:" + document.getElementById("sd").value + " usdl-core:hasEntityInvolvement sd:" + document.getElementById("sp_business_entity").value + "EntityInvolvement .")
            .add("sd:" + document.getElementById("sd").value + " usdl-core-cb:hasServiceModel sd:" + document.getElementById("sd_model").value + " .")
            // Create the service model instance
            .add("sd:" + document.getElementById("sd_model").value + " a bp:" + document.getElementById("bp_model").value + " .")
            .add("sd:" + document.getElementById("sd_model").value + " gr:isVariantOf bp:" + document.getElementById("bp_instance").value + " .")
            // Create and assign a profile instance to the service model
            .add("sd:" + document.getElementById("slp").value + " a bp:" + document.getElementById("slp_class").value + " .")
            .add("sd:" + document.getElementById("sd_model").value + " bp:has" + document.getElementById("slp_class").value + " sd:" + document.getElementById("slp").value + " .");
    //Create validFrom and validThrough
    kb_sd.add("sd:" + document.getElementById("sd").value + ' usdl-core-cb:validFrom ' + '"' + document.getElementById("valid_from_sd").value + '"^^xsd:date .');
    if (document.getElementById("valid_through_sd").value != "") {
        kb_sd.add("sd:" + document.getElementById("sd").value + ' usdl-core-cb:validThrough ' + '"' + document.getElementById("valid_through_sd").value + '"^^xsd:date .');
    }
    // Create successor of and recommendation deprecation, if any.
    // Deprecation recommendation will not be created if there is no successor of even if a value is provided!
    if (document.getElementById("successor_of_sd").value != "") {
        kb_sd.add("sd:" + document.getElementById("sd").value + " gr:successorOf <" + document.getElementById("successor_of_sd").value + "> .");
        if (document.getElementById("deprecation_recommendation_sd").value != "") {
            kb_sd.add("sd:" + document.getElementById("sd").value + ' usdl-core-cb:deprecationRecommendationTimePointSD ' + '"' + document.getElementById("deprecation_recommendation_sd").value + '"^^xsd:date .');
        }
    }
    // Add the chosen classification concepts to the service model
    kb_sd = addClassificationConceptsToSD(kb_sd);
    kb_sd = addQualServiceLevelsToSD(kb_sd);
    kb_sd = addQuantServiceLevelsToSD(kb_sd);
    // Dump the kb with the service description to a turtle format and show it in a separate window.
    var dmp = kb_sd.databank.dump({
        format: 'text/turtle',
        indent: true,
        serialize: true
    })
    return Base64.encode(dmp)
}