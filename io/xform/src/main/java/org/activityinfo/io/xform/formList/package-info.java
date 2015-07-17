/**
 * JAXB Mapping of the OpenRosa Form List Schema
 *
 * @see <a href="https://bitbucket.org/javarosa/javarosa/wiki/FormListAPI">OpenRosa FormList API</a>
 */
@XmlSchema(
        elementFormDefault = XmlNsForm.QUALIFIED,
        namespace = Namespaces.XFORM_LIST,
        xmlns = {
                @XmlNs(prefix = "", namespaceURI = Namespaces.XFORM_LIST),
        })
package org.activityinfo.io.xform.formList;

import org.activityinfo.io.xform.Namespaces;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;