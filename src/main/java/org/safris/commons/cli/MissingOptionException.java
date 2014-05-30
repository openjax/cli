/*  Copyright Safris Software 2008
 *  
 *  This code is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.safris.commons.cli;

public final class MissingOptionException extends OptionsException {
  private static final long serialVersionUID = 1752296328335010992L;

  public MissingOptionException() {
    super();
  }

  public MissingOptionException(final String message) {
    super();
  }

  public MissingOptionException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public MissingOptionException(final Throwable cause) {
    super(cause);
  }
}