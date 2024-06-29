package com.example.exe1;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopScoresFragment extends Fragment {

    private ListView topScoresListView;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_scores_list, container, false);
        topScoresListView = view.findViewById(R.id.topScoresListView);

        sharedPreferences = getActivity().getSharedPreferences("MyGamePrefs", getContext().MODE_PRIVATE);
        loadTopScores();

        topScoresListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String locationKey = "location_" + position;
                String storedLocation = sharedPreferences.getString(locationKey, "");
                Log.d("TopScoresFragment", "Retrieved location: " + storedLocation); // Debugging

                if (storedLocation != null && !storedLocation.isEmpty()) {
                    // Create bundle with location data
                    Bundle bundle = new Bundle();
                    bundle.putString("location", storedLocation);

                    // Initialize ScoreLocationsFragment and set arguments
                    ScoreLocationsFragment scoreLocationsFragment = new ScoreLocationsFragment();
                    scoreLocationsFragment.setArguments(bundle);

                    // Replace current fragment with ScoreLocationsFragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.mapView, scoreLocationsFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), "No location stored for this score", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void loadTopScores() {
        List<Integer> scores = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int score = sharedPreferences.getInt("distance_" + i, 0);
            if (score != 0) {
                scores.add(score);
            }
        }

        Collections.sort(scores, Collections.reverseOrder());

        List<String> scoreStrings = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            scoreStrings.add("Score " + (i + 1) + ": " + scores.get(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, scoreStrings);
        topScoresListView.setAdapter(adapter);
    }
}
