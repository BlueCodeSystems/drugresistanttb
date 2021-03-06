package zm.gov.moh.drugresistanttb.submodule.dashboard.patient.view;


import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import zm.gov.moh.common.base.BaseActivity;
import zm.gov.moh.common.model.VisitMetadata;
import zm.gov.moh.core.model.Key;
import zm.gov.moh.core.model.VisitState;
import zm.gov.moh.core.model.submodule.BasicModule;
import zm.gov.moh.core.model.submodule.Module;
import zm.gov.moh.core.repository.database.Database;
import zm.gov.moh.core.repository.database.entity.derived.Client;
import zm.gov.moh.core.utils.BaseApplication;
import zm.gov.moh.core.utils.Utils;
import zm.gov.moh.drugresistanttb.R;
import zm.gov.moh.drugresistanttb.base.BaseEventDrugResistantTbHandler;
import zm.gov.moh.drugresistanttb.databinding.ActivityDrugResistantTbPatientDashboardBinding;
import zm.gov.moh.drugresistanttb.submodule.dashboard.patient.adapter.MdrFormListAdapter;
import zm.gov.moh.drugresistanttb.submodule.dashboard.patient.model.FormGroup;
import zm.gov.moh.drugresistanttb.submodule.dashboard.patient.viewmodel.DrugResistantTbPatientDashboardViewModel;

public class DrugResistantTbPatientDashboardActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String PERSON_ID = "PERSON_ID";

    DrugResistantTbPatientDashboardViewModel viewModel;
    Bundle mBundle;
    Module vitals;
    long clientId;
    Client client;
    private BottomNavigationView bottomNavigationView;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    List<FormGroup> mdrFormLists = new ArrayList<>();
    private Bundle bundle;
    BaseEventDrugResistantTbHandler baseEventDrugResistantTbHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mBundle = getIntent().getExtras();
        baseEventDrugResistantTbHandler = new BaseEventDrugResistantTbHandler(this);
        setToolBarEventHandler(baseEventDrugResistantTbHandler);

        clientId = mBundle.getLong(PERSON_ID);
        viewModel = ViewModelProviders.of(this).get(DrugResistantTbPatientDashboardViewModel.class);

        viewModel.setBundle(mBundle);
        setViewModel(viewModel);
        AndroidThreeTen.init(this);

        vitals = ((BaseApplication) this.getApplication()).getModule(BaseApplication.CoreModule.VITALS);
        Database database = viewModel.getRepository().getDatabase();

        ActivityDrugResistantTbPatientDashboardBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_drug_resistant_tb_patient_dashboard);
        binding.setTitle("MDR Patient Dashboard");

        FormGroup formGroup = new FormGroup();
        List<BasicModule> list = new ArrayList<>();
        BasicModule formModule;

        formModule = new BasicModule("MDR Visits", DrugResistantTbVisitLinkActivity.class);

        list.add(formModule);

        formGroup.setTitle("MDR Forms");
        formGroup.setFormList(list);
        mdrFormLists.add(formGroup);

        // Create an adapter that knows which fragment should be shown on each page
        MdrFormListAdapter adapter = new MdrFormListAdapter(this, mdrFormLists, mBundle);
        ExpandableListView formListView = findViewById(R.id.mdr_adapter_list);
        formListView.setAdapter(adapter);


        getViewModel().getRepository().getDatabase().genericDao().getMdrPatientById(clientId)
            .observe(this, patient -> {
                if (patient == null) {
                    Toast.makeText(this, "Client not enrolled", Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
        });

        initToolBar(binding.getRoot());
        /*viewModel.getRepository().getDatabase().genericDao().getMdrPatientById(clientId).
                observe(this, binding::setClient);
        viewModel.getRepository().getDatabase().personAddressDao().findByPersonIdObservable(clientId).
                observe(this, binding::setClientAddress);
        viewModel.getRepository().getDatabase().locationDao().getByPatientId(clientId).
                observe(this, binding::setFacility);*/

        // adding patient information in databundle
        viewModel.getRepository().getDatabase().clientDao().findById(clientId)
                .observe(this, client1 -> {
                    //binding.setClient(client1);

                    mBundle.putString(Key.PERSON_FAMILY_NAME, client1.getFamilyName());
                    mBundle.putString(Key.PERSON_GIVEN_NAME, client1.getGivenName());
                    mBundle.putString(Key.PERSON_DOB, client1.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    this.getIntent().putExtras(mBundle);
                });

        initBundle(mBundle);

        // Set Bottom Navigation View Listener
        bottomNavigationView = findViewById(R.id.bottom_navigation_view1);

        bottomNavigationView.inflateMenu(R.menu.bottom_menu);
        fragmentManager = getSupportFragmentManager();
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.recents_menu_item_id);

        database.genericDao().getMdrPatientById(clientId).observe(this, binding::setClient);
        database.personAddressDao().findByPersonIdObservable(clientId).observe(this, binding::setClientAddress);
        database.locationDao().getByPatientId(clientId, 7L).observe(this, binding::setFacility);
        database.visitDao().getByPatientIdVisitTypeId(clientId, 11L, 12L, 12L).observe(this, viewModel::onVisitsRetrieved);


        //set navigation drawer
        addDrawer(this);

        /*BasicModule basicModule = new BasicModule("Bacteriological Exam",
                BacteriologicalExaminationActivity.class);
        try {
            JsonForm bacterialExam = new JsonForm("Bacteriological Exam",
                    Utils.getStringFromInputStream(this.getAssets().open("visits/mdr.json.json")));
            bundle.putSerializable(BaseActivity.JSON_FORM, bacterialExam);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }


    public void startVisit () {
        VisitMetadata visitMetadata = null;
        try {
            visitMetadata = new VisitMetadata(this,
                    Utils.getStringFromInputStream(this.getAssets().open("visits/mdr.json")));
            bundle.putSerializable(Key.VISIT_METADATA, visitMetadata);
            bundle.putSerializable(Key.VISIT_STATE, VisitState.NEW);
            this.startModule(BaseApplication.CoreModule.VISIT, bundle);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.recents_menu_item_id)
            fragment = new DrugResistantTbPatientDashboardRecentsViewPagerFragment();
        else if (id == R.id.insights_menu_item_id)
            fragment = new DrugResistantTbPatientDashboardInsightsViewPagerFragment();
        fragment.setArguments(mBundle);
        replaceFragment(fragment);
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.mdr_bottom_navigation_view_container, fragment).commit();
    }


    public Module getVitals() {
        return vitals;
    }

    public Client getClient() {
        return client;
    }

    public DrugResistantTbPatientDashboardViewModel getViewModel() {
        return viewModel;
    }
}





