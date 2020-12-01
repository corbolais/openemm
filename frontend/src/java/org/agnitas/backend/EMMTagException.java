/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import org.agnitas.util.Log;

/**
 * Exception to be raised in EMMTag
 */
public class EMMTagException extends Exception {
	final static long serialVersionUID = 0x055e44;

	/**
	 * Constructor
	 *
	 * @param data Reference to configuration
	 * @param msg  the error message
	 */
	public EMMTagException(Data data, EMMTag tag, String msg) {
		super(msg = (msg != null) && (tag != null) && (tag.mTagFullname != null) ? msg + ": " + tag.mTagFullname : msg);
		if ((data != null) && (msg != null)) {
			data.logging(Log.ERROR, "emmtag", msg);
		}
	}

	public EMMTagException(String msg) {
		this(null, null, msg);
	}
}
