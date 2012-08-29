/*******************************************************************************
 * Copyright (c) 2012 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

/**
 * A composite validator 'composes' the results of multiple validator by computing each
 * in order and only returning the worst result. If some results are equal in terms
 * of status priority then the first one is returned.
 * 
 * @author Kris De Volder
 */
public class CompositeValidator extends LiveExpression<ValidationResult> implements ValueListener<ValidationResult> {

	public CompositeValidator() {
		super(ValidationResult.OK);
	}

	private List<LiveExpression<ValidationResult>> elements = new ArrayList<LiveExpression<ValidationResult>>();
	
	public CompositeValidator addChild(LiveExpression<ValidationResult> child) {
		elements.add(child);
		child.addListener(this);
		return this;
	}
	
	@Override
	protected ValidationResult compute() {
		ValidationResult worst = ValidationResult.OK;
		for (LiveExpression<ValidationResult> v : elements) {
			ValidationResult r = v.getValue();
			if (r!=null && r.status>worst.status) {
				worst = r;
			}
			if (worst.status>=IStatus.ERROR) {
				//can't really get any worse than that
				return worst;
			}
		}
		return worst;
	}

	public void gotValue(LiveExpression<ValidationResult> exp, ValidationResult value) {
		refresh();
	}

}
