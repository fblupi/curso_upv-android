package com.example.mislugares;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, LocationListener {

    public BaseAdapter adaptador;
    private MediaPlayer mp;
    private LocationManager manejador;
    private Location mejorLocaliz;

    static final long DOS_MINUTOS = 2 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adaptador = new AdaptadorLugares(this);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adaptador);
        listView.setOnItemClickListener(this);

        mp = MediaPlayer.create(this, R.raw.audio);

        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            actualizaMejorLocaliz(manejador.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
        if(manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            actualizaMejorLocaliz(manejador.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        }
    }

    private void actualizaMejorLocaliz(Location localiz) {
        if (mejorLocaliz == null || localiz.getAccuracy() < 2*mejorLocaliz.getAccuracy() || localiz.getTime() - mejorLocaliz.getTime() > DOS_MINUTOS) {
            Log.d(Lugares.TAG, "Nueva mejor localización");
            mejorLocaliz = localiz;
            Lugares.posicionActual.setLatitud(localiz.getLatitude());
            Lugares.posicionActual.setLongitud(localiz.getLongitude());
        }
    }

    @Override protected void onStart() {
        super.onStart();
    }

    @Override protected void onResume() {
        super.onResume();
        mp.start();
        activarProveedores();
    }

    @Override protected void onPause() {
        super.onPause();
        mp.pause();
        manejador.removeUpdates(this);
    }

    @Override protected void onStop() {
        super.onStop();
    }

    @Override protected void onRestart() {
        super.onRestart();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle estadoGuardado){
        super.onSaveInstanceState(estadoGuardado);
        if (mp != null) {
            int pos = mp.getCurrentPosition();
            estadoGuardado.putInt("posicion", pos);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle estadoGuardado){
        super.onRestoreInstanceState(estadoGuardado);
        if (estadoGuardado != null && mp != null) {
            int pos = estadoGuardado.getInt("posicion");
            mp.seekTo(pos);
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true; /** true -> el menú ya está visible */
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings){
            lanzarPreferencias(null);
            return true;
        }
        if (id == R.id.acercaDe) {
            lanzarAcercaDe(null);
            return true;
        }
        if(id == R.id.menu_buscar) {
            lanzarVistaLugar(null);
            return true;
        }
        if (id==R.id.menu_mapa) {
            Intent i = new Intent(this, Mapa.class);
            startActivity(i);
        }
        return true; /** true -> consumimos el item, no se propaga*/
    }

    @Override
    public void onItemClick(AdapterView parent,View vista, int posicion, long id){
        Intent i = new Intent(this, VistaLugar.class);
        i.putExtra("id", id);
        startActivity(i);
    }

    @Override public void onLocationChanged(Location location) {
        Log.d(Lugares.TAG, "Nueva localización: " + location);
        actualizaMejorLocaliz(location);
    }


    @Override public void onProviderDisabled(String proveedor) {
        Log.d(Lugares.TAG, "Se deshabilita: "+proveedor);
        activarProveedores();
    }

    @Override    public void onProviderEnabled(String proveedor) {
        Log.d(Lugares.TAG, "Se habilita: "+proveedor);
        activarProveedores();
    }

    @Override
    public void onStatusChanged(String proveedor, int estado, Bundle extras) {
        Log.d(Lugares.TAG, "Cambia estado: "+proveedor);
        activarProveedores();
    }

    private void lanzarAcercaDe(View view){
        Intent i = new Intent(this, AcercaDe.class);
        startActivity(i);
    }

    private void lanzarPreferencias(View view) {
        Intent i = new Intent(this, Preferencias.class);
        startActivity(i);
    }

    private void lanzarVistaLugar(View view) {
        final EditText entrada = new EditText(this);
        entrada.setText("0");
        new AlertDialog.Builder(this)
                .setTitle(R.string.seleccion_lugar)
                .setMessage(R.string.seleccion_lugar_mensaje)
                .setView(entrada)
                .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(MainActivity.this, VistaLugar.class);
                        i.putExtra("id",Long.parseLong(entrada.getText().toString()));
                        startActivity(i);
                    }
                })
                .setNegativeButton(R.string.cancelar,null)
                .show();
    }

    private void activarProveedores() {
        if(manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            manejador.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20 * 1000, 5, this);
        }

        if(manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            manejador.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 10, this);
        }
    }
}
