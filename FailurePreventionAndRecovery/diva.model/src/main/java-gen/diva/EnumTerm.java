/**
 * Copyright 2014 SINTEF <brice.morin@sintef.no>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package diva;

import diva.visitors.Visitor;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Enum Term</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link diva.EnumTerm#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see diva.DivaPackage#getEnumTerm()
 * @model
 * @generated
 */
public interface EnumTerm extends VariableTerm {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' reference. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' reference isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Value</em>' reference.
	 * @see #setValue(EnumLiteral)
	 * @see diva.DivaPackage#getEnumTerm_Value()
	 * @model required="true"
	 * @generated
	 */
	EnumLiteral getValue();

	/**
	 * Sets the value of the '{@link diva.EnumTerm#getValue <em>Value</em>}'
	 * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Value</em>' reference.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(EnumLiteral value);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model annotation=
	 *        "http://www.eclipse.org/emf/2002/GenModel body='return visitor.visitEnumTerm(this, context);'"
	 *        annotation=
	 *        "kermeta body='do\nresult := visitor.visitEnumTerm(self, context)\nend' isAbstract='false'"
	 * @generated
	 */
	<C, R> R accept(Visitor<C, R> visitor, C context);

} // EnumTerm
