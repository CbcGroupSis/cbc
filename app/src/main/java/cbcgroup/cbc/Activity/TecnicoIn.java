package cbcgroup.cbc.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;

import cbcgroup.cbc.Clases.CBC;
import cbcgroup.cbc.R;

public class TecnicoIn extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "TecnicosInActivity";
    String URL = "http://tecnicos.cbcgroup.com.ar/test/app_android/v14/hoja_de_reparacion.php?";
    String URL2 = "http://tecnicos.cbcgroup.com.ar/test/app_android/v14/tecnicos.php";
    TextView serie,sector,fecha,modelo,inconveniente,fechaVencimiento;
    Button button;
    String nombre;
    Bundle extra;
    CBC cbc;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_tecnico_in );
        modelo=findViewById( R.id.tec_modelo );
        serie=findViewById( R.id.tec_subItem_serie );
        sector=findViewById( R.id.tec_subItem_sector);
        fecha=findViewById( R.id.tec_subItem_fecha);
        inconveniente=findViewById( R.id.tec_subItem_inconveniente);
        button=findViewById( R.id.ingresar );
        fechaVencimiento=findViewById( R.id.tec_subItem_fechaVencimiento );
        button.setOnClickListener( this );
        cbc= new CBC(TecnicoIn.this);
        extra=getIntent().getExtras();
        if(cbc.Internet()) completeInfo();
        else Toast.makeText( this,"NO TIENE ACCESO A INTERNET O SU CONNCECION ES MUY LENTA, VUELVA ATRAS Y INTENTE NUEVAMENTE",Toast.LENGTH_LONG ).show();
    }

    @Override
    public void onClick(View v)
    {
        if(v==button)
        {
            IngresoPedido();
        }
    }

    void completeInfo()
    {
        cbc.progressDialog( "Cargando Informacion...","Espere por favor..." );
        RequestQueue requestQueue = Volley.newRequestQueue(TecnicoIn.this);
        StringRequest stringRequest = new StringRequest( Request.Method.POST, URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String s)
                    {
                        cbc.progressDialogCancel();
                        Log.w(TAG,"Resp:"+s);
                        try
                        {
                            JSONObject response= new JSONObject( s );
                            JSONArray tecSubItem= response.getJSONArray( "hoja_reparacion" );
                            JSONObject obj=tecSubItem.getJSONObject(0 );
                            modelo.setText(obj.getString( "modelo" ));
                            serie.setText(obj.getString( "serie" ));
                            sector.setText(obj.getString( "sector" ));
                            fecha.setText(obj.getJSONObject( "fecha" ).getString("date"));
                            fechaVencimiento.setText(obj.getJSONObject( "fechaVence" ).getString("date") );
                            inconveniente.setText(obj.getString( "inconveniente" ) );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError)
                    {
                        cbc.progressDialogCancel();
                        Log.i( TAG,volleyError.toString());
                        Toast.makeText( TecnicoIn.this, "Error: "+volleyError.toString(), Toast.LENGTH_SHORT ).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new Hashtable<>();
                if(cbc.getdUserSector().equals( "super admin" )) nombre=extra.getString( "nameTecSa" );
                else nombre=cbc.getdUserName();
                params.put("Content-Type","application/json; charset=utf-8");
                params.put("name_tec",nombre );
                params.put("id_parte", extra.getString( "npedido" ));
                return params;
            }

        };
        requestQueue.add(stringRequest);
    }


    void IngresoPedido()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
        alertDialogBuilder.setMessage("Desea Ingresar al pedido tecnico?");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Si", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)

            {
                ingresoQuery();

            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Toast.makeText(TecnicoIn.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog=alertDialogBuilder.create();
        alertDialog.show();
    }

    void ingresoQuery()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(TecnicoIn.this);
        StringRequest stringRequest = new StringRequest( Request.Method.POST, URL2,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String s)
                    {
                        cbc.setIngresoTecnico(true);
                        cbc.setIngresonpedido( extra.getString( "npedido" ));
                        Toast.makeText( TecnicoIn.this, "Se ingreso correctamente!", Toast.LENGTH_SHORT ).show();
                        startActivity( new Intent(TecnicoIn.this,HomeActivity.class ).addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP ));
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError)
                    {
                        Log.i( TAG,volleyError.toString());
                        Toast.makeText( TecnicoIn.this, "Error: "+volleyError.toString(), Toast.LENGTH_SHORT ).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new Hashtable<>();
                params.put("Content-Type","application/json; charset=utf-8");
                params.put("llegada","true");
                params.put("id_tecnico",cbc.getdUserId() );
                params.put("serie",serie.getText().toString());
                params.put("id_parte", extra.getString( "npedido" ));
                return params;
            }

        };
        requestQueue.add(stringRequest);
    }

}
