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

import org.eclipse.emf.common.util.EList;

import diva.visitors.Visitor;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Configuration</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link diva.Configuration#getVariant <em>Variant</em>}</li>
 * <li>{@link diva.Configuration#getVerdict <em>Verdict</em>}</li>
 * </ul>
 * </p>
 *
 * @see diva.DivaPackage#getConfiguration()
 * @model
 * @generated
 */
public interface Configuration extends ScoredElement {
	/**
	 * Returns the value of the '<em><b>Variant</b></em>' containment reference
	 * list. The list contents are of type {@link diva.ConfigVariant}. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Variant</em>' containment reference list isn't
	 * clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Variant</em>' containment reference list.
	 * @see diva.DivaPackage#getConfiguration_Variant()
	 * @model containment="true"
	 * @generated
	 */
	EList<ConfigVariant> getVariant();

	/**
	 * Returns the value of the '<em><b>Verdict</b></em>' attribute. The
	 * literals are from the enumeration {@link diva.Verdict}. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Verdict</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Verdict</em>' attribute.
	 * @see diva.Verdict
	 * @see #setVerdict(Verdict)
	 * @see diva.DivaPackage#getConfiguration_Verdict()
	 * @model
	 * @generated
	 */
	Verdict getVerdict();

	/**
	 * Sets the value of the '{@link diva.Configuration#getVerdict
	 * <em>Verdict</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @param value
	 *            the new value of the '<em>Verdict</em>' attribute.
	 * @see diva.Verdict
	 * @see #getVerdict()
	 * @generated
	 */
	void setVerdict(Verdict value);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model annotation=
	 *        "http://www.eclipse.org/emf/2002/GenModel body='return visitor.visitConfiguration(this, context);'"
	 *        annotation=
	 *        "kermeta body='do\nresult := visitor.visitConfiguration(self, context)\nend' isAbstract='false'"
	 * @generated
	 */
	<C, R> R accept(Visitor<C, R> visitor, C context);

	/**
	 * @generated NOT
	 * @param v
	 */
	void addVariant(Variant v);

	/**
	 * @generated NOT
	 * @param ctx
	 */
	void computeScore(Context ctx);

	/**
	 * @generated NOT
	 * @param oracle
	 */
	void computeVerdicts(VariantExpression oracle);

	/**
	 * @generated NOT
	 */
	String id(VariabilityModel m);

} // Configuration
