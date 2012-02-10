/* 
 * Copyright (C) 2011 - 2012 Michi Gysel <michael.gysel@gmail.com>
 *
 * This file is part of the HSR Timetable.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.scythe.hsr;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import ch.scythe.hsr.entity.Day;
import ch.scythe.hsr.entity.Lesson;
import ch.scythe.hsr.entity.TimetableWeek;
import ch.scythe.hsr.enumeration.TimeUnit;
import ch.scythe.hsr.enumeration.WeekDay;

public class DayFragment extends DialogFragment {

	public static final String FRAGMENT_PARAMETER_DATA = "week";
	public static final String FRAGMENT_PARAMETER_WEEKDAY = "position";

	private TimetableWeek week;
	private WeekDay weekDay;
	private LayoutInflater layoutInflater;

	public DayFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle arguments = getArguments();
		if (arguments != null) {
			week = (TimetableWeek) (arguments.getSerializable(FRAGMENT_PARAMETER_DATA));
			weekDay = (WeekDay) (arguments.getSerializable(FRAGMENT_PARAMETER_WEEKDAY));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle instanceToSave) {
		super.onSaveInstanceState(instanceToSave);
		instanceToSave.putSerializable(FRAGMENT_PARAMETER_DATA, week);
		instanceToSave.putSerializable(FRAGMENT_PARAMETER_WEEKDAY, weekDay);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (savedInstanceState != null && savedInstanceState.containsKey(FRAGMENT_PARAMETER_DATA)) {
			week = (TimetableWeek) (savedInstanceState.getSerializable(FRAGMENT_PARAMETER_DATA));
			weekDay = (WeekDay) (savedInstanceState.getSerializable(FRAGMENT_PARAMETER_WEEKDAY));
		}

		layoutInflater = getLayoutInflater(savedInstanceState);
		View v = inflater.inflate(R.layout.timetable_day, container, false);
		TableLayout timeTable = (TableLayout) v.findViewById(R.id.timeTable);

		TextView headerCell = (TextView) v.findViewById(R.id.headerRow);
		headerCell.setText(getString(weekDay.getResourceReference()));

		updateTable(timeTable);

		return v;
	}

	private void updateTable(TableLayout timeTable) {
		Day day = week.getDay(weekDay);
		if (day != null) {
			Map<TimeUnit, List<Lesson>> lessons = day.getLessonsCompact();
			if (lessons.size() > 0) {
				for (Entry<TimeUnit, List<Lesson>> entry : lessons.entrySet()) {
					TimeUnit timeUnit = entry.getKey();
					List<Lesson> lessonsPerTimeUnit = entry.getValue();

					if (lessonsPerTimeUnit == null || lessonsPerTimeUnit.isEmpty()) {
						// no lessons at the given time slot
						createAndFormatTableRow(null, timeUnit, timeTable, layoutInflater);
					} else {
						for (Lesson lesson : lessonsPerTimeUnit) {
							createAndFormatTableRow(lesson, timeUnit, timeTable, layoutInflater);
						}
					}
				}
			} else {
				String message = getString(R.string.message_no_lessons);
				showInfoRow(timeTable, message);
			}
		} else {
			String message = getString(R.string.message_no_data);
			// TODO refactor this
			showInfoRow(timeTable, message);
		}
	}

	private void showInfoRow(TableLayout timeTable, String message) {
		TableRow descriptionRow = (TableRow) layoutInflater.inflate(R.layout.timetable_info_row, null);
		// TODO can we use an id here?
		TextView infoField = (TextView) descriptionRow.getChildAt(0);
		infoField.setText(message);
		timeTable.addView(descriptionRow);
	}

	public void updateDate(TimetableWeek week) {
		this.week = week;
		TableLayout timeTable = (TableLayout) getView().findViewById(R.id.timeTable);
		// remove all existing table rows and add them again
		timeTable.removeAllViews();
		updateTable(timeTable);
	}

	private void createAndFormatTableRow(Lesson lesson, TimeUnit timeUnit, TableLayout timeTable,
			LayoutInflater layoutInflater) {

		View row = layoutInflater.inflate(R.layout.timetable_row, null);

		TextView timeUnitField = (TextView) row.findViewById(R.id.rowTimeunit);
		TextView lessonField = (TextView) row.findViewById(R.id.rowLesson);
		TextView lecturerField = (TextView) row.findViewById(R.id.rowLecturer);
		TextView roomField = (TextView) row.findViewById(R.id.rowRoom);
		TextView descriptionField = (TextView) row.findViewById(R.id.rowDescription);
		TextView typeField = (TextView) row.findViewById(R.id.rowType);

		// fill values into row
		timeUnitField.setText(timeUnit.toDurationString(" - "));
		timeTable.addView(row);
		if (lesson != null) {
			lessonField.setText(lesson.getIdentifierShort());
			roomField.setText(lesson.getRoom());
			lecturerField.setText(lesson.getLecturersAsString(", "));
			typeField.setText(lesson.getType());

			if (lesson.hasDescription()) {
				descriptionField.setText(lesson.getDescription());
			} else {
				descriptionField.setVisibility(View.GONE);
			}
		} else {
			// TODO don't show anything (not even the time...) and merge with
			// the info row
			View secondRow = row.findViewById(R.id.secondRow);
			secondRow.setVisibility(View.GONE);
			descriptionField.setVisibility(View.GONE);
			typeField.setVisibility(View.GONE);
		}

	}

}