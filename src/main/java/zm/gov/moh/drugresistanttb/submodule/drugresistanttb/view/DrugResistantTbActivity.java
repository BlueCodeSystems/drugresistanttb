package zm.gov.moh.drugresistanttb.submodule.drugresistanttb.view;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import zm.gov.moh.common.ui.BaseActivity;
import zm.gov.moh.drugresistanttb.R;
import zm.gov.moh.drugresistanttb.BR;
import zm.gov.moh.drugresistanttb.databinding.ActivityDrugResistantTbBinding;
import zm.gov.moh.drugresistanttb.submodule.drugresistanttb.viewmodel.DrugResistantTbViewModel;

import android.os.Bundle;

public class DrugResistantTbActivity extends BaseActivity {

    DrugResistantTbViewModel drugResistantTbViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_drug_resistant_tb);

        drugResistantTbViewModel = ViewModelProviders.of(this).get(DrugResistantTbViewModel.class);
        ActivityDrugResistantTbBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_drug_resistant_tb);
        binding.setVariable(BR.mdrtbviewmodel, drugResistantTbViewModel);

        drugResistantTbViewModel.getStartSubmodule().observe(this, this::startModule);

        ToolBarEventHandler toolBarEventHandler = getToolbarHandler();
        toolBarEventHandler.setTitle("Multi-Drug Resistant Tuberculosis");
        binding.setToolbarhandler(toolBarEventHandler);
        binding.setContext(this);
    }
}
