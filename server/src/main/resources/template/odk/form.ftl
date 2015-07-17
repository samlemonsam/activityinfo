<?xml version="1.0" encoding="UTF-8"?>

<h:html xmlns="http://www.w3.org/2002/xforms"
        xmlns:h="http://www.w3.org/1999/xhtml"
        xmlns:jr="http://openrosa.org/javarosa">
    <h:head>
        <h:title>${name?xml}</h:title>

        <model>
            <instance>
                <data id="siteform">
                    <meta>
                    <instanceID/>
                    </meta>
                    <activity>${id?c}</activity>

                    <partner><#if partnerRange?size == 1>${partnerRange[0].id?c}</#if></partner>
                    <#if !locationType.nationwide >
                    <locationname/>
                    <gps/>
                    </#if>
                
                <#if reportingFrequency == 0> <#-- ActivityDTO.REPORT_ONCE -->
                    <date1>${.now?string("yyyy-MM-dd")}</date1>
                    <date2>${.now?string("yyyy-MM-dd")}</date2>
                    <#list indicators as indicator>
                        <I${indicator.id?c} />
                    </#list>
                </#if>

                <#list attributeGroups as attributeGroup>
                    <AG${attributeGroup.id?c} />
                </#list>

                    <comments/>
                </data>
            </instance>

            <bind nodeset="/data/meta/instanceID" type="string" readonly="true()" calculate="concat('uuid:',uuid())"/>
            <bind nodeset="/data/activity" required="true()"/>
            <bind nodeset="/data/partner" required="true()"/>
            <bind nodeset="/data/gps" type="geopoint" required="false()"/>
            <bind nodeset="/data/date1" type="date" required="true()"/>
            <bind nodeset="/data/date2" type="date" required="true()"
                  constraint=". >= /data/date1" jr:constraintMsg="date must be on or after /data/date1"/>

        <#if reportingFrequency == 0>
            <#list indicators as indicator>
                <#if indicator.type.id == "FREE_TEXT">
                    <bind nodeset="/data/I${indicator.id?c}" type="string" ${indicator.mandatory?string("required=\"true()\"", "")}/>
                <#elseif indicator.type.id == "QUANTITY">
                    <bind nodeset="/data/I${indicator.id?c}" type="decimal" ${indicator.mandatory?string("required=\"true()\"", "")}/>
                <#elseif indicator.type.id == "NARRATIVE">
                    <bind nodeset="/data/I${indicator.id?c}" type="string" ${indicator.mandatory?string("required=\"true()\"", "")}/>
                <#else>
                    <bind nodeset="/data/I${indicator.id?c}" type="string" ${indicator.mandatory?string("required=\"true()\"", "")}/>
                </#if>
            </#list>
        </#if>

        <#list attributeGroups as attributeGroup>
            <#if attributeGroup.mandatory>
                <bind nodeset="/data/AG${attributeGroup.id?c}" required="true()"/>
            </#if>
        </#list>
        </model>
    </h:head>

<h:body>
<#if (partnerRange?size > 1)>
    <select1 ref="/data/partner">
        <label>${label.odkPartner?xml}</label>
        <#list partnerRange as partner>
            <item>
                <label>${partner.name?xml}</label>
                <value>${partner.id?c}</value>
            </item>
        </#list>
    </select1>
</#if>

    <#if !locationType.nationwide >
    <group>
        <label>${locationType.name?xml}</label>
        <input ref="/data/locationname">
        <label>${label.odkLocationName?xml}</label>
        </input>
        <input ref="/data/gps">
        <label>${label.odkLocationCoordinates?xml}</label>
        </input>
    </group>
    </#if>

    <group>
        <input ref="/data/date1">
        <label>${label.odkStartDate?xml}</label>
        </input>
        <input ref="/data/date2">
        <label>${label.odkEndDate?xml}</label>
        </input>
    </group>

    <#list fields as field>

        <#if field.entityName == "Indicator">
            <input ref="/data/I${field.id?c}">
            <label>${field.name?xml}</label>
            <#if field.description?? >
            <hint>${field.description}</hint>
            </#if>

            </input>
        </#if>

        <#if field.entityName == "AttributeGroup">
            <${field.multipleAllowed?string("select", "select1")} ref="/data/AG${field.id?c}">
            <label>${field.name?xml}</label>
            <#list field.attributes as attribute>
                <item>
                    <label>${attribute.name?xml}</label>
                    <value>${attribute.id?c}</value>
                </item>
            </#list>
        </${field.multipleAllowed?string("select", "select1")}>

        </#if>

    </#list>

    <input ref="/data/comments">
    <label>${label.odkComments?xml}</label>
    </input>
    </h:body>
</h:html>
