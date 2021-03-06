package umn.ac.mecinan.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.flags.impl.DataUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import umn.ac.mecinan.R;
import umn.ac.mecinan.model.Mail;
import umn.ac.mecinan.model.Project;
import umn.ac.mecinan.model.Propose;
import umn.ac.mecinan.model.User;

public class ProposeProjectActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    final String TAG = "propose_project";

    Button btn_cancel, btn_propose;
    private String field;
    private String category;

    private DatabaseReference mDatabase;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser curr_user = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propose_project);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_propose = findViewById(R.id.btn_propose);

        Spinner spinnePurposerField = findViewById(R.id.spinner_propose_Field);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.field_select, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnePurposerField.setAdapter(adapter);
        spinnePurposerField.setOnItemSelectedListener(this);

        btn_cancel.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProposeProjectActivity.this, SearchActivity.class);
                finish();
                startActivity(intent);
            }
        });

        btn_propose.setOnClickListener(new View.OnClickListener() {
            TextView textname = findViewById(R.id.tv_propose_Name);
            TextView textduration = findViewById(R.id.tv_propose_Duration);
            TextView textprice = findViewById(R.id.tv_propose_Price);
            TextView textdesc = findViewById(R.id.tv_propose_Desc);
            TextView texttitle = findViewById(R.id.tv_propose_Title);

            @Override
            public void onClick(View v) {
                Boolean isEmpty = false;
                EditText inputname = findViewById(R.id.et_propose_Name);
                EditText inputduration = findViewById(R.id.et_propose_Duration);
                EditText inputprice = findViewById(R.id.et_propose_Price);
                EditText inputdesc = findViewById(R.id.et_propose_Desc);
                EditText inputtitle = findViewById(R.id.et_propose_Title);

                String name = inputname.getText().toString().trim();
                String duration = inputduration.getText().toString().trim();
                int price = Integer.parseInt(inputprice.getText().toString().trim());
                String desc = inputdesc.getText().toString().trim();
                String title = inputtitle.getText().toString().trim();

                /** Name Propose Field Validation **/
                if(TextUtils.isEmpty(name)) {
                    textname.setTextColor(getResources().getColor(R.color.brink_pink));
                    isEmpty = true;
                } else {
                    textname.setTextColor(getResources().getColor(R.color.black));
                }

                /** Ttile Propose Field Validation **/
                if(TextUtils.isEmpty(title)) {
                    texttitle.setTextColor(getResources().getColor(R.color.brink_pink));
                    isEmpty = true;
                } else {
                    texttitle.setTextColor(getResources().getColor(R.color.black));
                }

                /** Duration Propose Field Validation **/
                if(TextUtils.isEmpty(duration)) {
                    textduration.setTextColor(getResources().getColor(R.color.brink_pink));
                    isEmpty = true;
                } else {
                    textduration.setTextColor(getResources().getColor(R.color.black));
                }

                /** Fee Propose Field Validation **/
                if(inputprice.getText().toString().trim().length() < 0) {
                    textprice.setTextColor(getResources().getColor(R.color.brink_pink));
                    isEmpty = true;
                } else {
                    textprice.setTextColor(getResources().getColor(R.color.black));
                }

                /** Desc Propose Field Validation **/
                if(TextUtils.isEmpty(desc)) {
                    textdesc.setTextColor(getResources().getColor(R.color.brink_pink));
                    isEmpty = true;
                } else {
                    textdesc.setTextColor(getResources().getColor(R.color.black));
                }

                if(isEmpty) {
                    Toast.makeText(getApplicationContext(), "Fill the required field", Toast.LENGTH_SHORT).show();
                }

                if(!isEmpty) {
                    String idClient = curr_user.getUid();
                    int status = 0;
                    float rating = 5;

                    Intent intent = getIntent();
                    String idEmployee = intent.getStringExtra("idemployee");
                    String idProject = mDatabase.push().getKey();
                    //String idProject = null;

                    Project project = new Project(
                            idProject,
                            title,
                            idEmployee,
                            idClient,
                            field,
                            category,
                            desc,
                            price,
                            status,
                            rating
                    );

                    mDatabase.child("project").child(idProject).setValue(project);


                    /**
                     * Set variable for mail
                     */
                    Mail mail = new Mail();
                    mail.setMailType(1);
                    mail.setMailIsRead(false);
                    mail.setMailCategory("Work");
                    mail.setMailTitle("Project Proposal");
                    mail.setMailContent(curr_user.getEmail() + " Has proposed a new project to You. Please check and consider to accept or reject it.");
                    mail.setMailReceivedDate(null);
                    mail.setProjectName(title);
                    mail.setMailRecipient(idEmployee);
                    mail.setMailSender(curr_user.getUid());
                    mail.setIdProject(idProject);
                    mail.setProjectName(title);
                    mail.setProjectField(field);
                    mail.setProjectCategory(category);
                    mail.sendMail(mail);

                    finish();
                }

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.spinner_propose_Field) {
            field = parent.getItemAtPosition(position).toString();

            if(position == 0){
                spinnerCatIT();
            }
            if(position == 1){
                spinnerCatAD();
            }
            if(position == 2){
                spinnerCatBU();
            }
            if(position == 3){
                spinnerCatPR();
            }
            Log.d(TAG, field);
        }

        if(parent.getId() == R.id.spinner_propose_Category) {
            category = parent.getItemAtPosition(position).toString();
            Log.d(TAG, category);
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "nothing selected");
    }

    public void spinnerCatIT(){
        Spinner spinnerProposeCategory = findViewById(R.id.spinner_propose_Category);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.cat_it, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProposeCategory.setAdapter(adapter1);
        spinnerProposeCategory.setOnItemSelectedListener(this);
    }

    public void spinnerCatAD(){
        Spinner spinnerProposeCategory = findViewById(R.id.spinner_propose_Category);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.cat_ad, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProposeCategory.setAdapter(adapter1);
        spinnerProposeCategory.setOnItemSelectedListener(this);
    }

    public void spinnerCatBU(){
        Spinner spinnerProposeCategory = findViewById(R.id.spinner_propose_Category);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.cat_bu, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProposeCategory.setAdapter(adapter1);
        spinnerProposeCategory.setOnItemSelectedListener(this);
    }

    public void spinnerCatPR(){
        Spinner spinnerProposeCategory = findViewById(R.id.spinner_propose_Category);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.cat_pr, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProposeCategory.setAdapter(adapter1);
        spinnerProposeCategory.setOnItemSelectedListener(this);
    }
}
