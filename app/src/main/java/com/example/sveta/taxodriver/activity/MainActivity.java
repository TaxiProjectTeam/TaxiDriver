package com.example.sveta.taxodriver.activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.fragment.AboutProgramFragment;
import com.example.sveta.taxodriver.fragment.OrdersListFragment;
import com.example.sveta.taxodriver.fragment.UserInfoFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragments_container, new OrdersListFragment()).commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withDrawerWidthDp(240)
                .withToolbar(toolbar)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.text_orders_list_drawer),
                        new PrimaryDrawerItem().withName(R.string.text_info_about_user),
                        new PrimaryDrawerItem().withName(R.string.text_about_program)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //Replace fragments
                        switch (position){
                            case 1:
                                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragments_container,new OrdersListFragment()).commit();
                                return false;
                            case 2:
                                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragments_container,new UserInfoFragment()).commit();
                                return false;
                            case 3:
                                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragments_container,new AboutProgramFragment()).commit();
                                return false;
                            default:
                                return true;
                        }
                    }
                }).build();

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout_menu_item:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this,LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }
}
